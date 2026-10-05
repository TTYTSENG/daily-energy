param(
 [string]$SdkRoot="$env:LOCALAPPDATA\Android\Sdk",
 [string]$JdkRoot=$env:JAVA_HOME,
 [ValidateSet('Debug','Release')][string]$SigningMode='Debug',
 [string]$KeystorePath,
 [string]$KeyAlias,
 [string]$OutputDirectory=(Join-Path $PSScriptRoot 'dist')
)
$ErrorActionPreference='Stop'
if(!$JdkRoot){$JdkRoot=Join-Path $env:ProgramFiles 'Android\Android Studio\jbr'}
if(!(Test-Path -LiteralPath (Join-Path $JdkRoot 'bin\javac.exe'))){throw '找不到JDK，請使用 -JdkRoot 指定含bin/javac.exe的目錄。'}
if(!(Test-Path -LiteralPath (Join-Path $SdkRoot 'build-tools'))){throw '找不到Android SDK，請使用 -SdkRoot 指定已安裝的SDK。'}
$tools=Get-ChildItem (Join-Path $SdkRoot 'build-tools') -Directory | Where-Object {$_.Name -match '^\d+\.\d+\.\d+$'} | Sort-Object {[version]$_.Name} -Descending | Select-Object -First 1
$platform=Get-ChildItem (Join-Path $SdkRoot 'platforms') -Directory | Where-Object {$_.Name -match '^android-\d+(\.\d+)?$'} | Sort-Object {[version](($_.Name -replace '^android-','')+'.0')} -Descending | Select-Object -First 1
if(!$tools -or !$platform -or [version](($platform.Name -replace '^android-','')+'.0') -lt [version]'35.0'){throw '需要Android SDK Platform API35以上及穩定版Build Tools。'}
if($SigningMode -eq 'Release'){
 if(!$KeystorePath -or !(Test-Path -LiteralPath $KeystorePath) -or !$KeyAlias){throw 'Release須指定既有 -KeystorePath 與 -KeyAlias，不會自動產生發布金鑰。'}
 if(!$env:ENERGY_KEYSTORE_PASSWORD -or !$env:ENERGY_KEY_PASSWORD){throw '請設定 ENERGY_KEYSTORE_PASSWORD 與 ENERGY_KEY_PASSWORD；不要把密碼寫入原始碼。'}
}
$build=Join-Path $PSScriptRoot 'build'
$androidJar=Join-Path $platform.FullName 'android.jar'
$java=Join-Path $JdkRoot 'bin\java.exe'
function Run-Tool($path,$arguments){& $path @arguments;if($LASTEXITCODE -ne 0){throw "Tool failed: $path ($LASTEXITCODE)"}}
foreach($dir in @($build,(Join-Path $build 'classes'),(Join-Path $build 'generated'),(Join-Path $build 'dex'),$OutputDirectory)){New-Item -ItemType Directory -Path $dir -Force | Out-Null}
Run-Tool (Join-Path $tools.FullName 'aapt2.exe') @('compile','--dir',(Join-Path $PSScriptRoot 'app\src\main\res'),'-o',(Join-Path $build 'resources.zip'))
Run-Tool (Join-Path $tools.FullName 'aapt2.exe') @('link','-o',(Join-Path $build 'base.apk'),'-I',$androidJar,'--manifest',(Join-Path $PSScriptRoot 'app\src\main\AndroidManifest.xml'),'--java',(Join-Path $build 'generated'),'-A',(Join-Path $PSScriptRoot 'app\src\main\assets'),(Join-Path $build 'resources.zip'))
$sources=@(Get-ChildItem (Join-Path $PSScriptRoot 'app\src\main\java') -Recurse -Filter '*.java' | ForEach-Object {$_.FullName})+@(Get-ChildItem (Join-Path $build 'generated') -Recurse -Filter '*.java' | ForEach-Object {$_.FullName})
Run-Tool (Join-Path $JdkRoot 'bin\javac.exe') (@('-encoding','UTF-8','--release','8','-classpath',$androidJar,'-d',(Join-Path $build 'classes'))+$sources)
Run-Tool (Join-Path $JdkRoot 'bin\jar.exe') @('cf',(Join-Path $build 'classes.jar'),'-C',(Join-Path $build 'classes'),'.')
Run-Tool $java @('-cp',(Join-Path $tools.FullName 'lib\d8.jar'),'com.android.tools.r8.D8','--lib',$androidJar,'--min-api','26','--output',(Join-Path $build 'dex'),(Join-Path $build 'classes.jar'))
Copy-Item (Join-Path $build 'base.apk') (Join-Path $build 'unsigned.apk') -Force
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip=[IO.Compression.ZipFile]::Open((Join-Path $build 'unsigned.apk'),[IO.Compression.ZipArchiveMode]::Update)
try{[void][IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip,(Join-Path $build 'dex\classes.dex'),'classes.dex',[IO.Compression.CompressionLevel]::Optimal)}finally{$zip.Dispose()}
Run-Tool (Join-Path $tools.FullName 'zipalign.exe') @('-f','-p','4',(Join-Path $build 'unsigned.apk'),(Join-Path $build 'aligned.apk'))
[xml]$manifest=Get-Content -LiteralPath (Join-Path $PSScriptRoot 'app\src\main\AndroidManifest.xml') -Raw
$version=$manifest.manifest.GetAttribute('versionName','http://schemas.android.com/apk/res/android')
$apk=Join-Path $OutputDirectory "DailyEnergy-$version-$($SigningMode.ToLower()).apk"
if($SigningMode -eq 'Debug'){
 $debugDir=Join-Path $PSScriptRoot 'signing';New-Item -ItemType Directory -Force -Path $debugDir | Out-Null
 $key=Join-Path $debugDir 'debug.keystore'
 # Standard local debug password; this key is generated per developer and ignored by Git.
 if(!(Test-Path -LiteralPath $key)){Run-Tool (Join-Path $JdkRoot 'bin\keytool.exe') @('-genkeypair','-keystore',$key,'-alias','debug','-storepass','android','-keypass','android','-keyalg','RSA','-keysize','2048','-validity','10000','-dname','CN=Local Debug','-storetype','JKS')}
 $signArgs=@('--ks',$key,'--ks-key-alias','debug','--ks-pass','pass:android','--key-pass','pass:android')
}else{$signArgs=@('--ks',$KeystorePath,'--ks-key-alias',$KeyAlias,'--ks-pass','env:ENERGY_KEYSTORE_PASSWORD','--key-pass','env:ENERGY_KEY_PASSWORD')}
Run-Tool $java (@('-jar',(Join-Path $tools.FullName 'lib\apksigner.jar'),'sign')+$signArgs+@('--out',$apk,(Join-Path $build 'aligned.apk')))
Run-Tool $java @('-jar',(Join-Path $tools.FullName 'lib\apksigner.jar'),'verify','--verbose',$apk)
Write-Output "APK: $apk"
