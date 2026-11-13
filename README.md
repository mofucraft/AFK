# AFK
/afk [reason]： 離席を表示し、LuckPermsでAFKグループを付与

## 放置の判定（v1.4〜）
- 活動とみなすのは「ブロックをまたぐ移動」と「視点の向きの変化（config の look-threshold 度以上・checking-interval ごとに合計を空にする）」だけ。
- クリック・攻撃・チャット・コマンドは数えない。動かずに自動連打していると set-afk-interval 分で離席になる。
