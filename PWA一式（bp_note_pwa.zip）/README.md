# 血圧記録ノート　PWA・Androidアプリ化 手順書

## フォルダの中身
| ファイル | 役割 |
|---|---|
| index.html | アプリ本体（カラフル版＋PWA対応） |
| manifest.webmanifest | アプリ名・アイコン・色などの設定 |
| sw.js | オフラインで開けるようにする仕組み（Service Worker） |
| icons/ | アプリのアイコン（通常・マスク用・iPhone用） |
| android-webview/MainActivity.kt | Android Studioで作る場合のサンプルコード |

> フォルダ構成は変えずに、そのまま公開してください。

---

## A. PWAとして公開する（GitHub Pagesの例・無料）
1. https://github.com でアカウントを作成。
2. 右上「＋」→「New repository」→ 名前を `bp-note` などにし、**Public** で作成。
3. 「uploading an existing file」から、このフォルダの中身（index.html、manifest.webmanifest、sw.js、icons フォルダ）をまとめてドラッグ →「Commit changes」。
4. 「Settings」→「Pages」→ Branch を `main` / `/(root)` にして「Save」。
5. 1〜2分後、`https://（ユーザー名）.github.io/bp-note/` で開けます。

※ OneDrive・SharePointではPWAとして動きません（HTTPSのWebサーバーが必要）。
※ 記録は各端末の中だけに保存され、サーバーには送られません。

## B. Androidにインストール（PWA）
1. Androidの **Chrome** で公開URLを開く。
2. 一度画面を操作して表示を確認（初回でオフライン用データが保存されます）。
3. 画面下に「アプリをインストール」が出たらタップ。出ない場合は右上「︙」→「アプリをインストール」（または「ホーム画面に追加」→「インストール」）。
4. ホーム画面・アプリ一覧に「血圧ノート」アイコンが追加されます。
5. アイコンから起動すると、アドレスバーなしの全画面で動きます。機内モードでも起動を確認しておくと安心です。

**今までの記録を引き継ぐ**
1. 今使っている版で「⚙️ 設定・出力」→「CSV・保存」→「バックアップ保存」。
2. インストールしたアプリで同じ画面の「復元する」→ 保存したJSONを選ぶ。

**アンインストール**：アイコン長押し →「アンインストール」（記録も消えるので先にバックアップを）。

**アプリを更新したとき**：sw.js の `bp-note-v1` を `v2` に変えて index.html と一緒にアップロード → アプリを2回開き直すと反映されます。

---

## C. APKを作る方法①：PWABuilder（おすすめ・プログラミング不要）
1. 上のAで公開し、URLをChromeで開けることを確認。
2. https://www.pwabuilder.com を開き、公開URLを入力して「Start」。
3. 診断結果を確認（manifest・Service Worker・アイコンは対応済み）。
4. 「Package For Stores」→ **Android** →「Generate Package」。
   - Package ID：`com.（自分の名前）.bpnote` など
   - App name：血圧記録ノート
   - Signing key：「Create new」（初回）
5. ZIPがダウンロードされます。中身：
   - `*.apk`：自分の端末に直接入れる用
   - `*.aab`：Google Play提出用
   - `signing.keystore` と `signing-key-info.txt`：**更新時に必須。必ず保管**
   - `assetlinks.json`：アドレスバーを消すための証明ファイル
6. `assetlinks.json` を公開サイトの `/.well-known/assetlinks.json` に置く。
   - GitHub Pagesでは**ドメイン直下**に置く必要があるため、`（ユーザー名）.github.io` という名前のリポジトリで公開するか、Netlify・Cloudflare Pages等を使います。
   - 置かない場合も動きますが、上部にURLバーが表示されます。

※ この方式のAPKは中身を公開URLから読み込みます（初回だけネット必須、以後はオフライン可）。

## D. APKを作る方法②：Android Studio（WebViewに同梱・完全オフライン）
1. Android Studio をインストール（https://developer.android.com/studio）。
2. 「New Project」→「Empty Views Activity」→ Language：**Kotlin**、Package name：`com.example.bpnote`、Minimum SDK：API 24。
3. 左のツリーで `app/src/main` を右クリック →「New」→「Folder」→「Assets Folder」。
4. `assets` に **index.html と icons フォルダ** をコピー（sw.js・manifestは不要ですが入れても問題なし）。
5. `MainActivity.kt` を、同梱の `android-webview/MainActivity.kt` の内容に置き換え（1行目のpackage名は自分のものに合わせる）。
6. アイコン：`res` を右クリック →「New」→「Image Asset」→ `icons/icon-512.png` を指定。
7. アプリ名：`res/values/strings.xml` の `app_name` を「血圧記録ノート」に。
8. 実機テスト：Androidで「開発者向けオプション」→「USBデバッグ」をON、USB接続して ▶（Run）。
9. APK作成：メニュー「Build」→「Generate Signed App Bundle / APK」→「APK」→ 新しいキーストアを作成（**ファイルとパスワードは必ず保管**）→「release」→ 完了。
   `app/release/app-release.apk` ができます。

この方式では、CSV・バックアップは端末の「ダウンロード」フォルダに保存されます（index.html側に対応処理を入れてあります）。

---

## E. APKをAndroidにインストール
1. APKをスマホへ送る（USBコピー、Googleドライブ、OneDrive など）。
2. スマホの「Files」アプリなどでAPKをタップ。
3. 「提供元不明のアプリ」の警告が出たら「設定」→ そのアプリ（Files・Chrome等）の **「この提供元のアプリを許可」** をON → 戻る。
4. 「インストール」→ Google Play プロテクトの確認が出たら「詳細」→「インストールする」。
5. 完了後、ホーム画面のアイコンから起動。インストールが終わったら、3の許可はOFFに戻すと安全です。

※ 会社支給スマホでは、管理ポリシーによりAPKのインストールが禁止されている場合があります。その場合は B（PWA）を使うか、情報システム部門に確認してください。

## F. Google Playで配布する場合
- Google Play Console の開発者登録（有料・本人確認あり）
- `.aab` ファイルを提出、プライバシーポリシー、健康アプリの申告、審査が必要
- 個人の新規アカウントでは、公開前にテスター参加による一定期間のテストが求められます

## 困ったとき
| 症状 | 対処 |
|---|---|
| 「インストール」が出ない | HTTPSで開いているか確認。Chromeで開いているか確認。ページを再読み込み |
| 記録が見えない | 開いている場所（URL・アプリ）が変わると別保存。バックアップ→復元 |
| 更新が反映されない | sw.js のバージョン番号を上げる。アプリを完全に閉じて2回開き直す |
| APKが入らない | 提供元不明アプリの許可、会社端末の制限、空き容量を確認 |
