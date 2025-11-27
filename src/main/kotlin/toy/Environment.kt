package toy

class Environment(val enclosing: Environment? = null) {
    private val values = HashMap<String, Any?>()

    fun define(name: String, value: Any?) {
        if (values.containsKey(name)) {
            throw RuntimeException("Variable '$name' already defined.")
        }
        values[name] = value
    }

    fun get(name: String): Any? {
        if (values.containsKey(name)) {
            return values[name]
        }
        if (enclosing != null) {
            return enclosing.get(name)
        }
        throw RuntimeException("Variable '$name' is not defined.")
    }

    fun assign(name: String, value: Any?): Any? {
        if (values.containsKey(name)) {
            values[name] = value
            return value
        }
        if (enclosing != null) {
            return enclosing.assign(name, value)
        }
        throw RuntimeException("Undefined variable '$name'.")
    }
}
