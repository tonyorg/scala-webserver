load("@io_bazel_rules_scala//scala:scala.bzl", "scala_library")

scala_library(
    name = "graphql",
    srcs = glob(["*.scala"]),
    scalacopts = ["-deprecation"],
    visibility = ["//visibility:public"],
    exports = [
        "//3rdparty/jvm/org/parboiled",
    ],
    deps = [
        "//3rdparty/jvm/com/github/etaty:rediscala",
        "//3rdparty/jvm/org/postgresql",
        "//3rdparty/jvm/org/sangria_graphql:sangria",
        "//3rdparty/jvm/org/sangria_graphql:sangria_marshalling_api",
        "//server/src/auth",
        "//server/src/dal",
        "//server/src/dal-write",
        "//server/src/game",
        "//server/src/marshalling/game-redis",
        "//server/src/util",
    ],
)
