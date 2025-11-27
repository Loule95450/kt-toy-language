package toy

import java.io.File

val interpreter = Interpreter()

fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        runFile(args[0])
    } else {
        repl()
    }
}

fun runFile(path: String) {
    val source = File(path).readText()
    run(source)
}

fun run(source: String) {
    try {
        val lexer = Lexer(source)
        val tokens = lexer.tokenize()

        val parser = Parser(tokens)
        val ast = parser.parse()

        interpreter.interpret(ast)
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}

fun repl() {
    println("Toy Language REPL")
    println("Type 'exit' to quit\n")

    while (true) {
        try {
            print(">>> ")
            val line = readlnOrNull()
            if (line == null || line.trim() == "exit") {
                break
            }
            run(line)
        } catch (e: Exception) {
            break
        }
    }
}
