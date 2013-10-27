package com.confluex.mule.dist.util

class Artifact {
    String path
    Map<String, String> pom
    byte[] jar

    String getName() {
        path.substring(path.lastIndexOf('/') + 1)
    }
}
