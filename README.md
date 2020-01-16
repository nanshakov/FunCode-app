# FunCode-app
https://funcodechallenge.com/task

`
-- auto-generated definition
create table metaV2
(
    urlImgHash    String,
    sourceUrl     String,
    contentHash   String,
    source        String,
    datetime      DateTime,
    pathToContent String,
    likes         Nullable(Int32),
    dislikes      Nullable(Int32),
    comments      Nullable(Int32),
    alt           Nullable(String),
    author        Nullable(String)
)
    engine = ENGINE MergeTree() PARTITION BY toYYYYMM(datetime) ORDER BY (urlImgHash, contentHash) SETTINGS index_granularity=8192;
`
