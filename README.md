# spk-mod — Minecraft Forge 1.20.1 用のペットMOD

## 概要

- Minecraft Forge 1.20.1(Java 17・公式マッピング)向けのMOD
- 手懐けるとついてくるペットMOB `SpkPet` を追加する
- 挙動はWolf相当(手懐け・追従・餌での回復)
- モデルはBlockbenchで作成(`spk_pet.bbmodel` / `spk_pet.java`)

## 必要環境

- Java 17
- gradle(ラッパー同梱:`./gradlew`)

## ビルド

```sh
./gradlew build      # JARを生成
./gradlew runClient  # 開発クライアントを起動
```

## 制限

- お座り・野生スポーン・繁殖は対象外(出現はスポーンエッグのみ)

## ライセンス

未設定。
