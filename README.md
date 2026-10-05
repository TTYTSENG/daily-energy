# 每日能量 Daily Energy

繁體中文、離線使用的 Android 熱量平衡紀錄 App。內建90項食品與42項活動，支援自由新增飲食、飲料與活動項目，並依個人資料估算每日能量缺口。

## 功能

- 食品支援每100g、每100mL及每份／整杯；可填蛋白質、調理方式與額外油糖。
- 活動可設定耗能率、時長及選填心率，區分基準日與額外活動。
- 基礎代謝納入性別、年齡、身高及體重；提供燃脂、增肌、維持方向。
- 每日紀錄自動保存；歷史保留當時的個人資料與項目數值。
- 7天、30天及全部日期累積，CSV彙總及完整JSON備份匯出／匯入。
- 不需登入，沒有廣告、分析SDK或伺服器同步。

## 安裝與使用

最低Android 8.0（API26）。維護者可將簽署後的APK放到GitHub Releases，下載後在手機上安裝。

首次開啟先到「設定」填個人資料，再到「今日」新增紀錄；確認整天記錄完整後才顯示缺口。「項目」可新增、修改或隱藏內容。移除App或清除資料會刪除本機紀錄，請定期匯出JSON。CSV只含彙總，不能取代完整備份。

官方食品值採每100g可食部分；飲料請秤去冰、實際喝入的重量，不將mL直接當g。自訂飲料可依品牌每100mL或整杯營養標示設定。成品已含的油糖與配料不重複添加。

## 建置 APK

目前使用Windows PowerShell直接呼叫Android SDK工具，無Gradle／Maven執行期相依套件。需要JDK 17以上、Android SDK Platform API35以上及穩定版Build Tools。可透過Android Studio安裝工具，但這不是可直接以Gradle開啟的Android Studio專案。

```powershell
./build.ps1 -SdkRoot 'C:\Android\Sdk' -JdkRoot 'C:\Java\jdk'
```

預設產生本地測試簽章與 `dist/DailyEnergy-1.0.0-debug.apk`。`signing/`、`build/`及`dist/`已列入`.gitignore`。發布版使用既有私鑰及環境變數，詳見[發布與上傳說明](docs/PUBLISHING.md)。

**簽章不同的APK無法直接覆蓋安裝。** 若已安裝原先的私人APK，要保留資料並更新，應使用原本私下保管的簽章私鑰建置Release；改用新Debug金鑰時，先匯出備份，再移除舊App、安裝新App並匯入。不要把私鑰、真實紀錄或備份加入公開儲存庫。

## 測試

Node.js 22以上。計算測試無需安裝套件：

```sh
node --test tests/engine.test.mjs
```

選用的介面測試需Playwright及Chrome；測試在獨立瀏覽器環境執行，使用合成資料：

```sh
npm install
npm run test:ui
```

未安裝Chrome時，可執行 `npx playwright install chromium` 並以 `ENERGY_TEST_BROWSER=chromium` 執行。測試結果寫入被忽略的 `test-results/`。GitHub Actions會自動執行無相依套件的計算測試，不發布APK、不使用簽章私鑰。

原版本已完成27項計算及介面測試，並於Android API37模擬器驗證安裝、啟動、關閉再開後資料保存及系統文件選擇器的JSON匯出／匯入。最低API26依建置設定，尚未在Android 8.0實體裝置驗證。

## 計算與來源

[計算方式](docs/CALCULATIONS.md)說明BMR、基準倍率、活動淨增量、飲食及心率強度；[資料來源](DATA_SOURCES.md)保留原始數值來源、資料處理與適用限制。

每日消耗＝BMR×基準倍率＋額外活動淨增量。缺口＝每日消耗－飲食攝取。熱量、心率與短期體重均為參考，無法保證燃脂量或肌肉增加。日本BMR公式及國際成人活動值不是全亞洲族群的專屬校正。成人MET以19～59歲為主，60歲以上可能有較大偏差。孕哺／特殊健康狀況不套本公式；BMI低於18.5不設定燃脂目標。

## 原始碼結構

```text
app/src/main/
  AndroidManifest.xml
  java/tw/personal/energy/MainActivity.java  # Android容器、私有儲存、檔案選擇器
  assets/index.html                        # 本機介面
  assets/app.js                            # 紀錄、項目、歷史及備份
  assets/engine.js                         # 計算與驗證
  assets/catalog.js                        # 有來源註記的內建參考資料
  assets/style.css
  res/drawable/ic_launcher.xml
tests/
docs/
build.ps1
```

## 授權與隱私

本專案自行撰寫的程式碼與文件採[MIT](LICENSE)。第三方食品、活動參考資料及其來源說明不由MIT重新授權，詳見[第三方聲明](THIRD_PARTY_NOTICES.md)。沒有包含原始PDF、網站圖像、機關標誌或外部文件全文。

資料保留於手機App私有儲存，使用者自行匯出的檔案需自行保管。來源連結由系統瀏覽器開啟。詳見[隱私說明](PRIVACY.md)。

貢獻前請閱讀[CONTRIBUTING](CONTRIBUTING.md)，不要在Issue或PR附上真實個人備份。
