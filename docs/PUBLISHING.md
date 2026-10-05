# 上傳GitHub與發布APK

## 第一次上傳

1. 解壓縮公開版ZIP，打開`daily-energy`資料夾。
2. 在GitHub建立空白儲存庫，例如`daily-energy`。本資料夾已有README、MIT LICENSE及`.gitignore`，不必再以GitHub範本建立第二份。
3. 可用GitHub的Add file → Upload files上傳資料夾內檔案，或使用以下Git方式。
4. 確認`.github/workflows/test.yml`、`.gitignore`及`.gitattributes`也有上傳。不要只把ZIP上傳為儲存庫內容。

```sh
git init
git add .
git status
git commit -m "Initial Daily Energy app"
git branch -M main
git remote add origin https://github.com/YOUR_ACCOUNT/daily-energy.git
git push -u origin main
```

請將YOUR_ACCOUNT換成自己的帳號。此包沒有附`.git`歷史、私鑰、個人備份或建置產物。`.gitignore`提供日後保護，但不會替已提交的私鑰清除歷史。

## Debug與Release

預設建置會在本機`signing/debug.keystore`建立獨立Debug金鑰；密碼是Android一般開發慣例的`android`，並非此專案既有私人簽章金鑰。簽章檔整個目錄被Git忽略。

正式更新用你保管的既有金鑰，不要將它複製至GitHub。Release要求Keystore、alias及兩個環境變數，不會自動建立新的發布私鑰。可在PowerShell用不回顯的提示讀入密碼：

```powershell
$secureStorePassword = Read-Host 'Keystore password' -AsSecureString
$secureKeyPassword = Read-Host 'Key password' -AsSecureString
$env:ENERGY_KEYSTORE_PASSWORD = [System.Net.NetworkCredential]::new('', $secureStorePassword).Password
$env:ENERGY_KEY_PASSWORD = [System.Net.NetworkCredential]::new('', $secureKeyPassword).Password
try {
    ./build.ps1 -SdkRoot 'C:\Android\Sdk' -JdkRoot 'C:\Java\jdk' `
      -SigningMode Release -KeystorePath 'C:\Private\release.keystore' -KeyAlias 'YOUR_ALIAS'
} finally {
    Remove-Item Env:ENERGY_KEYSTORE_PASSWORD -ErrorAction SilentlyContinue
    Remove-Item Env:ENERGY_KEY_PASSWORD -ErrorAction SilentlyContinue
}
```

範例路徑與alias請換成自己的值。原先私用版本的簽章仍留在原本私人專案內，未包含於此公開包；保留它才能直接更新原APK。環境變數只在目前程序使用，不要將密碼寫入腳本或版本控制。

## GitHub Releases

後續發布前提高`AndroidManifest.xml`的`versionCode`及`versionName`、更新package.json與文件，再用相同私鑰建置Release。到GitHub Releases建立版本並附上簽署後的APK、SHA256與變更說明；不要附上Keystore或包含Keystore的舊原始碼ZIP。

本包附的GitHub Actions僅測試計算，不自動建置或公開APK。若日後增加Release工作流程，將私鑰與密碼放GitHub Secrets並限制發布權限，不能寫進儲存庫。

## 授權範圍

自行撰寫程式與文件使用MIT。第三方資料適用DATA_SOURCES與THIRD_PARTY_NOTICES的來源條件，不宣稱所有外部數據均由MIT授權。
