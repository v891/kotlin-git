fun main(args: Array<String>) {
    
    if (args.size < 3 || args.size > 3) {
        println("Invalid number of program arguments")
        return
    }

    args.forEachIndexed { index, s -> println("Argument ${index + 1}: $s. It has ${s.length} characters") }
}
