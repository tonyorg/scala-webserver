load("@io_bazel_rules_scala//scala:scala_import.bzl", "scala_import")
scala_import(
    name = "parboiled",
    jars = [
        "//external:jar/org/parboiled/parboiled_2_12"
    ],
    runtime_deps = [
        "//3rdparty/jvm/com/chuusai:shapeless_2_12",
        "//3rdparty/jvm/org/scala_lang:scala_library"
    ],
    visibility = [
        "//visibility:public"
    ]
)


