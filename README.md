# 前景離開警告測試

這是一個 Android 原生測試 App。當主畫面完全離開前景、進入背景時，會要求前景服務播放警告音。

## 自動建置 APK

GitHub Actions 會在推送到 main/master 時自動建置，也可以在 Actions 頁面手動執行 **Build foreground warning test APK**。

成功後，在該次工作流程的 **Artifacts** 區域下載 `foreground-warning-test-apk`，解壓縮即可取得 `app-debug.apk`。

## 專案結構

Android Gradle 專案檔與 Kotlin 原始碼均直接存放在儲存庫，不再於 CI 執行期間臨時產生。

## 測試限制

此程式僅測試 App 進入背景時的警告音請求，不會阻止使用者切換 App，也不能保證所有 Android 廠牌都允許背景音效播放。實際行為仍須在目標手機上測試。
