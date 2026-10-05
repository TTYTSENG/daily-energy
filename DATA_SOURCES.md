# 內建資料來源

查核日期：2026-10-05。內建資料檔：`app/src/main/assets/catalog.js`。保留來源ID／連結、數值單位及適用備註。程式碼MIT授權不取代來源資料條款。

## 食品（90項）

來源：[食品營養成分資料集，衛生福利部食品藥物管理署](https://data.gov.tw/dataset/8543)、[營養資料查詢](https://consumer.fda.gov.tw/Food/TFND.aspx?nodeID=178)。原資料標示[政府資料開放授權條款第1版](https://data.gov.tw/license)；亦參照[食藥署網站資料開放宣告](https://www.fda.gov.tw/TC/opendata.aspx)。

本專案由資料集擷取90項食品的每100g熱量、蛋白質、脂肪及碳水化合物，保留TFDA整合編號與樣品前處理描述。部分名稱加上台灣常用名稱，並自行補充可食重量、調理油糖及飲料使用提示。未提供的營養素維持空白；不把未知蛋白質當0。這是選取、整理後的衍生資料，並非TFDA官方App或即時完整資料庫。

去冰全糖／半糖／微糖奶茶為特定取樣結果，不代表各店配方一致。飲料單位仍是每100g，不直接換算杯容量。成品已含的糖、油與配料不能重複加。

## 台灣活動耗能

來源：[國民健康署運動消耗卡洛里](https://www.hpa.gov.tw/Pages/Detail.aspx?nodeid=571&pid=9738&sid=14174)，採官方表的kcal/kg/時參考值。使用條件：[國健署政府網站資料開放宣告](https://www.hpa.gov.tw/Pages/Detail.aspx?nodeid=92&pid=5141&sid=5140)、[政府資料開放授權條款第1版](https://data.gov.tw/license)。

本專案選取個別活動數值，重新撰寫簡短標籤及登錄提醒。此表為台灣官方採用的集團參考，不是全亞洲族群按性別與年齡分層的實測校正。

## 國際成人活動補充值

來源：[2024成人活動彙編日本公開譯本](https://www.nibn.go.jp/activities/documents/2024Compendium_table_adult_ver1_1_5.pdf)，及其原典[2024 Adult Compendium](https://pacompendium.com/)。其中煮菜、學習、家事、伸展、彼拉提斯、固定單車及按速度分級的跳繩等補充個別數值。兩項重量訓練值參考原典conditioning-exercise項目。

本專案僅整理個別數值、來源代碼及自行撰寫的中文活動標籤／提示，不附原PDF、完整表格排版、原文說明全文或圖像。日本公開譯本的來源代碼保留於備註。數值用1 MET約1 kcal/kg/時的近似；成人版以19～59歲為主，沒有任意性別倍率，也不是亞洲專屬校正。

日本研究機構的[使用條款](https://www.nibn.go.jp/en/use-of-content.html)說明數值及簡單表格的使用方式、來源註記及第三方權利；本專案保留原典與日本版本出處，未將第三方內容另行聲明為MIT。若日後加入原文、PDF、圖像或完整表格，需另確認原典的授權條件。

## 方法參考（僅連結，不附全文）

- [日本Ganpule基礎代謝估算](https://www.nibn.go.jp/eiken/hn/modules/kisotaisya/)
- [Tanaka等最大心率研究](https://pubmed.ncbi.nlm.nih.gov/11153730/)
- [ACSM 2011活動與強度建議](https://pubmed.ncbi.nlm.nih.gov/21694556/)
- [ACSM 2015蛋白質衛教](https://www.acsm.org/docs/default-source/files-for-resource-library/protein-intake-for-optimal-muscle-maintenance.pdf)

1.2基準倍率、300kcal燃脂缺口、200kcal增肌盈餘及1.6g/kg蛋白質為可調整的起始設定，不是上述機構指定人人相同的處方。
