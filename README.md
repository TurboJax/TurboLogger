# TurboLogger
Really just a simple logger I've thrown together over the past few years.  Allows logging and retrieval of values over the NetworkTables protocol for FRC with some useful adjustments.

## Links:
- Vendordep URL: [https://turbojax.org/files/TurboLogger.json](https://turbojax.org/files/TurboLogger.json)
- Javadocs: [https://turbojax.org/static/javadocs/turbologger/index.html](https://turbojax.org/static/javadocs/turbologger/index.html)
- Github: [https://git.turbojax.org/TurboLogger](https://git.turbojax.org/TurboLogger)

## Usage:
- Just put `import org.turbojax.TurboLogger` at the top of your file and log away!
- All of the logging functions are static, so no need to make an instance of them.
- At the moment, TurboLogger does not support logging wpiunits so convert them to a number before logging.

## Aliases
Aliases are essentially shorter forms of keys.  Rather than referencing "/motors/motor1/outputs/voltage" every time you want to read the voltage, you can make an alias called "m1voltage" and read/write to that instead.  
Aliases have a separate lastRead time from their parent key and any other aliases of that key.  This allows you to update a value then have multiple aliases to listen for changes in different places.  

To make an alias, use `TurboLogger#addAlias(String ntPath, String alias)`.  
This will create an alias for the NetworkTables path (ntPath) provided.  

You can remove an alias with `TurboLogger#removeAlias(String alias)`.  
This only removes the individual alias, it does not affect the parent key or any other aliases of that key.  

## Functions of TurboLogger
`TurboLogger.enableDataLogs(path)` &rarr; Starts logging to datalogs.  The parameter is the path to create the datalog file at.  
`TurboLogger.disableDataLogs()` &rarr; Disables datalog logging.  
`TurboLogger.log(key, value)` &rarr; Logs the value to NetworkTables under the key parameter.  The aliases vararg allows you to define aliases when you push a value without needing to run `TurboLogger.addAliases()`.  Supports logging of all primitive data types, Strings, StructSerializable objects, and arrays of each of them.  Returns nothing and marks the value as unread.  
`TurboLogger.get(key, defaultValue)` &rarr; Returns an object/primitive that matches the type of the defaultValue.  (It's why the function can be called simply "get" over "getBoolean" and others.)  Marks the value as read.  Supports all the same classes that the log function does.  
`TurboLogger.addAlias(key, alias)` &rarr; Registers a new alias as a reference to the key.  See above.  
`TurboLogger.removeAlias(alias)` &rarr; Removes an alias.  See above.  
`TurboLogger.hasChanged(key)` &rarr; Gets if the value of the key has changed.  This returns true if the user has logged a value to the key since the last time it was read, or if the variable changes in NetworkTables.  
`TurboLogger.remove(key)` &rarr; Removes a NetworkTables path and all of its aliases from TurboLogger.  If the key provided is an alias, it finds the parent path and removes it and its aliases.  

### Quick Examples:
```java
// Logs the string "Value logged" to the "String/1" position in NetworkTables
TurboLogger.log("String/1", "Value logged");

// Assigns "alias1" as an alias to the NetworkTables path "String/1"
TurboLogger.addAlias("String/1", "alias1");

// Gets the string from the ntPath
// If there was no published value or the type of the published value was not a string, it'd return "DefaultVal".
// Since we pushed a value above, this will return "Value Logged"
TurboLogger.get("String/1", "DefaultVal");

// Gets the string from the alias
// Since we registered "alias1" as an alias to "String/1", this would return "Value logged".
TurboLogger.get("alias1", "DefaultVal");

// Checks if the value was changed since we last read it.  This will return "false".
TurboLogger.hasChanged("String/1");

// Changing the logged value to "New Value".
TurboLogger.log("alias1", "New Value");

// Now hasChanged() will return true for both the key and alias.
TurboLogger.hasChanged("String/1");
TurboLogger.hasChanged("alias1");

// However if we read the value using the alias...
TurboLogger.get("alias1", "DefaultVal");

// Then the alias will return false, but the path will remain true.
// This functionality is included so that you can have multiple aliases listening to one value and allow each of them to update.  (Like 4 PID controllers using the same kP, kI, and kD values).
TurboLogger.hasChanged("String/1");
TurboLogger.hasChanged("alias1");

// A quick way to add multiple aliases is to make an array or list and run a for each loop on it.
// Array form:
String[] newAliases = {"alias2", "alias3"};
for (String alias : newAliases) {
    TurboLogger.addAlias("String/1", alias);
}

// List form:
List.of("alias4", "alias5").forEach(alias -> TurboLogger.addAlias("String/1", alias));

// If you ever feel the need to remove some aliases, turn to TurboLogger.removeAlias().  It removes just the alias you provide and nothong else.
TurboLogger.removeAlias("alias2");

// For mass removal, just do the same thing as with TurboLogger#addAlias

// Lastly we have the remove() function, which removes a ntPath and all of its aliases from the logger.
// You can pass an alias into this function as well, and it'll still work.
TurboLogger.remove("alias1");
```