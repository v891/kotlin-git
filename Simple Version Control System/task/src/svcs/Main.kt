package svcs

import java.io.File
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.security.MessageDigest


private fun fileWithDirectoryAssurance(directory: String, filename: String): File {
    val dir = File(directory)
    if (!dir.exists()) dir.mkdirs()
    return File("$directory/$filename")
}

fun main(args: Array<String>) {
    val charset: Charset = StandardCharsets.UTF_8

    val dir = File("commits")
    if (!dir.exists()) dir.mkdirs()

    val config: File = fileWithDirectoryAssurance("vcs", "config.txt")
    val index: File = fileWithDirectoryAssurance("vcs", "index.txt")
    val log: File = fileWithDirectoryAssurance("vcs", "log.txt")
    config.createNewFile()
    index.createNewFile()
    log.createNewFile()

    val helpMap = mapOf(
        Pair("config", "Get and set a username."),
        Pair("add", "Add a file to the index."),
        Pair("log", "Show commit logs."),
        Pair("commit", "Save changes."),
        Pair("checkout", "Restore a file.")
    )


    if (args.isEmpty() || args[0] == "--help") {
        println("These are SVCS commands:")
        helpMap.forEach { println("${it.key}        ${it.value}") }
        return
    } else if (helpMap.containsKey(args[0]) && args.size == 1) {
        when {
            args[0] == "config" -> {
                val userName: String = if (config.readLines().isEmpty()) "" else config.readLines()[0]
                if (userName.isNotBlank()) println("The username is $userName.") else println(
                    "Please, tell me who " +
                            "you are."
                )
            }
            args[0] == "add" -> {
                var content = String(Files.readAllBytes(index.toPath()), charset)

                if (content.isNullOrBlank())
                    println(helpMap[args[0]])
                else {
                    println("Tracked files:")
                    println(content.trim())
                }
            }
            args[0] == "log" -> {
                var content = String(Files.readAllBytes(log.toPath()), charset)
                if (content.isNullOrBlank())
                    println("No commits yet.")
                else {
                    content.trim().split(System.getProperty("line.separator"))
                        .asReversed()
                        .forEach { println(it) }

                }
            }
            args[0] == "commit" -> println("Message was not passed.")
            args[0] == "checkout" -> println("Commit id was not passed.")
            else -> println(helpMap[args[0]])
        }
        return
    } else if (helpMap.containsKey(args[0]) && args.size > 1) {
        when {
            args[0] == "config" -> {

                Files.write(config.toPath(), args[1].toByteArray(charset))
                println("The username is ${args[1]}.")
            }
            args[0] == "add" -> {
                var file = File(args[1])
                if (file.exists()) {

                    index.appendText("\n${args[1]}", charset)
                    println("The file \'${args[1]}\' is tracked.")
                } else {
                    println("Can't found '${args[1]}'.")
                }
            }

            args[0] == "commit" -> {
                var content = String(Files.readAllBytes(index.toPath()), charset)
                val fileNames = content.trim().split(System.getProperty("line.separator"))
                String(Files.readAllBytes(index.toPath()), charset)
                val joinToString: String = fileNames.map { File(it) }
                    .joinToString { String(Files.readAllBytes(it.toPath()), charset) }


                val md = MessageDigest.getInstance("SHA3-256")
                val hash1: ByteArray = md.digest(joinToString.toByteArray(charset))

                val hash = bytesToHex(hash1)
                val dir = File("vcs/commits/$hash")
                if (!dir.exists()) {
                    dir.mkdirs()
                    fileNames.forEach {
                        File(it).copyTo(File("vcs/commits/$hash/$it"), true, DEFAULT_BUFFER_SIZE)
                    }
                    val userName: String = if (config.readLines().isEmpty()) "" else config.readLines()[0]

                    log.appendText("\n${args[1]}", charset)
                    log.appendText("\nAuthor: $userName", charset)
                    log.appendText("\ncommit $hash", charset)
                    log.appendText("\n", charset)

                    println("Changes are committed.")
                    return
                } else {
                    println("Nothing to commit.")
                    return
                }
            }

            args[0] == "checkout" -> {
                var content = String(Files.readAllBytes(index.toPath()), charset)
                val fileNames = content.trim().split(System.getProperty("line.separator"))
                val dir = File("vcs/commits/${args[1]}")
                if (dir.exists()) {
                    fileNames.forEach {
                        File("vcs/commits/${args[1]}/$it").copyTo(File(it), true, DEFAULT_BUFFER_SIZE)
                    }
                    println("Switched to commit ${args[1]}.")
                } else {
                    println("Commit does not exist.")
                }
            }

        }
        return
    } else {
        println("\'${args[0]}\' is not a SVCS command.")
        return
    }
}

fun bytesToHex(bytes: ByteArray): String? {
    val sb = StringBuilder()
    for (b in bytes) {
        sb.append(String.format("%02x", b))
    }
    return sb.toString()
}