package org.turbojax;

import edu.wpi.first.networktables.*;
import edu.wpi.first.util.struct.Struct;
import edu.wpi.first.util.struct.StructSerializable;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.DriverStation;
import java.io.File;
import java.util.HashMap;
import java.util.List;

public class TurboLogger {
    // Hashmaps for NT logging
    private static final HashMap<String, Long> lastReads = new HashMap<>();
    private static final HashMap<String, String> aliasToNTPath = new HashMap<>();

    private static final NetworkTableInstance instance = NetworkTableInstance.getDefault();
    private static final NetworkTable table = instance.getTable("TurboLogger");

    /**
     * Enables DataLog recording of NT output.
     *
     * <p>
     * The wpilog is created at the logPath point. If the logPath ends with a
     * forward slash, it is seen as a directory. Otherwise, it is seen as a file.
     *
     * @param logPath The path to store the logfile at.
     */
    public static void enableDataLogs(String logPath) {
        File file = new File(logPath);

        // Checking if the file is a directory
        if (file.isDirectory()) {
            DataLogManager.start(logPath);
        } else {
            String parentDir = file.getParent();
            if (parentDir == null)
                parentDir = "";

            DataLogManager.start(parentDir, file.getName());
        }

        DataLogManager.logNetworkTables(true);
    }

    /** Closes the existing DataLog file and resets it to null. */
    public static void disableDataLogs() {
        // Stopping the logger from pulling from NetworkTables.
        DataLogManager.logNetworkTables(false);
    }

    // Error messages

    /**
     * Reports when a publisher or subscriber have inconsistent types with what is
     * being logged or read.
     *
     * @param key   The key being logged to or read.
     * @param type  The type being logged.
     * @param isPub Whether or not the issue occurred with a publisher.
     */
    private static void subTypeMismatch(String key, String type) {
        if (aliasToNTPath.containsKey(key)) {
            System.out.println("Error: Cannot subscribe values to the alias \"" + key + "\" of key \""
                    + aliasToNTPath.get(key) + "\" as it does not handle objects of type " + type + ".");
        } else {
            System.out.println("Error: Cannot subscribe values to the key \"" + key
                    + "\" as it does not handle objects of type " + type + ".");
        }
    }

    /**
     * Sets the lastReads value of a NetworkTables path and all its aliases to 0.
     * 
     * @param ntPath The NetworkTables path to rest. CANNOT be an alias.
     */
    private static void resetLastReads(String ntPath) {
        // Restting the lastRead entry for the ntPath and its aliases
        lastReads.put(ntPath, 0L);

        aliasToNTPath.forEach((alias, path) -> {
            if (ntPath.equals(path)) {
                lastReads.put(alias, 0L);
            }
        });
    }

    /**
     * Gets the ntPath from an arbitray key. The key should be either an alias or a
     * ntPath.
     * 
     * @param key The key to get the ntPath from.
     * 
     * @return If the key is an alias, it returns the ntPath the key points to.
     *         Otherwise it returns the key.
     */
    private static String getNTPathFromKey(String key) {
        // Checking if the key is an alias
        if (aliasToNTPath.containsKey(key)) {
            return aliasToNTPath.get(key);
        }

        return key;
    }

    // Loggers

    private static void genericLog(String key, NetworkTableValue value) {
        String ntPath = getNTPathFromKey(key);

        // Getting the topic
        Topic topic = table.getTopic(ntPath);

        // Making sure the existing topic's type does not conflict with the one being
        // logged.
        if (!topic.getTypeString().equals("") && !topic.getTypeString().equals(value.getType().getValueStr())) {
            System.out.println("Error: Cannot publish values to the key \"" + key + "\" as it only accepts objects of type " + topic.getTypeString() + ".");
            return;
        }

        // Pushing the value
        GenericPublisher pub = topic.genericPublish(value.getType().getValueStr());
        pub.set(value);

        // Resetting the lastRead entry for the key and its aliases
        resetLastReads(ntPath);
    }

    private static NetworkTableValue genericGet(String key, NetworkTableValue defaultValue) {
        String ntPath = key;

        // Checking if the key is an alias or not
        if (aliasToNTPath.containsKey(key)) {
            ntPath = aliasToNTPath.get(key);
        }

        Topic topic = table.getTopic(ntPath);

        // Making sure the existing topic's type does not conflict with the one being logged.
        if (!topic.getTypeString().equals("") && !topic.getTypeString().equals(defaultValue.getType().getValueStr())) {
            System.out.println("Error: Cannot pull values from the subsciber the key \"" + key + "\" points to as it returns objects of type " + topic.getTypeString() + ".");
            return defaultValue;
        }

        // Updating the lastReads entry
        lastReads.put(key, System.currentTimeMillis());

        return topic.genericSubscribe().get();
    }

    /**
     * Logs a boolean array to NetworkTables.
     *
     * @param key   The key to log the value under. This can be a NetworkTables path
     *              or an alias.
     * @param value The boolean array to log.
     */
    public static void log(String key, boolean[] value) {
        genericLog(key, NetworkTableValue.makeBooleanArray(value));
    }

    /**
     * Logs a boolean to NetworkTables.
     *
     * @param key   The key to log the value under. This can be a NetworkTables path
     *              or an alias.
     * @param value The boolean to log.
     */
    public static void log(String key, boolean value) {
        genericLog(key, NetworkTableValue.makeBoolean(value));
    }

    /**
     * Logs a double array to NetworkTables.
     *
     * @param key   The key to log the value under. This can be a NetworkTables path
     *              or an alias.
     * @param value The double array to log.
     */
    public static void log(String key, double[] value) {
        genericLog(key, NetworkTableValue.makeDoubleArray(value));
    }

    /**
     * Logs a double to NetworkTables.
     *
     * @param key   The key to log the value under. This can be a NetworkTables path
     *              or an alias.
     * @param value The double to log.
     */
    public static void log(String key, double value) {
        genericLog(key, NetworkTableValue.makeDouble(value));
    }

    /**
     * Logs a float array to NetworkTables.
     *
     * @param key   The key to log the value under. This can be a NetworkTables path
     *              or an alias.
     * @param value The float array to log.
     */
    public static void log(String key, float[] value) {
        genericLog(key, NetworkTableValue.makeFloatArray(value));
    }

    /**
     * Logs a float to NetworkTables.
     *
     * @param key   The key to log the value under. This can be a NetworkTables path
     *              or an alias.
     * @param value The float to log.
     */
    public static void log(String key, float value) {
        genericLog(key, NetworkTableValue.makeFloat(value));
    }

    /**
     * Logs an int array to NetworkTables.
     *
     * @param key   The key to log the value under. This can be a NetworkTables path
     *              or an alias.
     * @param value The int array to log.
     */
    public static void log(String key, int[] value) {
        // Converting the int array to a long array
        long[] new_value = new long[value.length];
        for (int i = 0; i < value.length; i++) {
            new_value[i] = value[i];
        }

        genericLog(key, NetworkTableValue.makeIntegerArray(new_value));
    }

    /**
     * Logs an int to NetworkTables.
     *
     * @param key   The key to log the value under. This can be a NetworkTables path
     *              or an alias.
     * @param value The int to log.
     */
    public static void log(String key, int value) {
        genericLog(key, NetworkTableValue.makeInteger(value));
    }

    /**
     * Logs a string array to NetworkTables.
     *
     * @param key   The key to log the value under. This can be a NetworkTables path
     *              or an alias.
     * @param value The string array to log.
     */
    public static void log(String key, String[] value) {
        genericLog(key, NetworkTableValue.makeStringArray(value));
    }

    /**
     * Logs a string to NetworkTables.
     *
     * @param key   The key to log the value under. This can be a NetworkTables path
     *              or an alias.
     * @param value The string to log.
     */
    public static void log(String key, String value) {
        genericLog(key, NetworkTableValue.makeString(value));
    }

    /**
     * Logs a struct array to NetworkTables.
     *
     * @param key   The key to log the value under. This can be a NetworkTables path
     *              or an alias.
     * @param value The struct array to log.
     * @param <T>   An object to log that implements {@link StructSerializable}.
     */
    @SuppressWarnings("unchecked")
    public static <T extends StructSerializable> void log(String key,
            T[] value) {
        String ntPath = getNTPathFromKey(key);

        // Finding the struct for this StructSerializable object.
        Struct<T> struct = null;
        try {
            struct = (Struct<T>) value.getClass().getComponentType()
                    .getDeclaredField("struct").get(value);
        } catch (IllegalAccessException | NoSuchFieldException err) {
            DriverStation.reportError(
                    "No public instance of struct for the StructSerializable object "
                            + value.getClass().getName(),
                    err.getStackTrace());
            return;
        }

        // Getting the StructArrayTopic
        StructArrayTopic<T> topic = table.getStructArrayTopic(key, struct);

        // Making sure the topic points to the right type of StructSerializable object.
        // This will be false if the key has already been used under a different name.
        if (!topic.getTypeString().equals(struct.getTypeString())) {
            System.out.println("Error: Cannot publish values to the key \"" + key + "\" as it only accepts objects of type " + topic.getTypeString() + ".");
            return;
        }

        // Pushing the value
        topic.publish().set(value);

        // Resetting the lastRead entry for the key and its aliases
        resetLastReads(ntPath);
    }

    /**
     * Logs a struct to NetworkTables.
     *
     * @param key   The key to log the value under. This can be a NetworkTables path
     *              or an alias.
     * @param value The struct to log.
     * @param <T>   An object to log that implements {@link StructSerializable}.
     */
    @SuppressWarnings("unchecked")
    public static <T extends StructSerializable> void log(String key, T value) {
        String ntPath = getNTPathFromKey(key);

        // Finding the struct for this StructSerializable object.
        Struct<T> struct = null;

        try {
            struct = (Struct<T>) value.getClass().getDeclaredField("struct")
                    .get(value);
        } catch (IllegalAccessException | NoSuchFieldException err) {
            DriverStation.reportError(
                    "No public instance of struct for the StructSerializable object "
                            + value.getClass().getName(),
                    err.getStackTrace());
            return;
        }

        // Getting the StructTopic
        StructTopic<T> topic = table.getStructTopic(ntPath, struct);

        // Making sure the topic points to the right type of StructSerializable object.
        // This will be false if the key has already been used under a different name.
        if (!topic.getTypeString().equals(struct.getTypeString())) {
            System.out.println("Error: Cannot publish values to the key \"" + key + "\" as it only accepts objects of type " + topic.getTypeString() + ".");
            return;
        }

        // Pushing the value
        topic.publish().set(value);

        // Resetting the lastRead entry for the key and its aliases
        resetLastReads(ntPath);
    }

    // Getters

    /**
     * Gets a boolean array from NetworkTables.
     *
     * @param key          The key to find the value under.
     * @param defaultValue The value to return if the subscriber doesn't exist.
     *
     * @return The boolean array referenced by the key
     */
    public static boolean[] get(String key, boolean[] defaultValue) {
        return genericGet(key, NetworkTableValue.makeBooleanArray(defaultValue)).getBooleanArray();
    }

    /**
     * Gets a boolean from NetworkTables.
     *
     * @param key          The key to find the value under.
     * @param defaultValue The value to return if the subscriber doesn't exist.
     *
     * @return The boolean referenced by the key.
     */
    public static boolean get(String key, boolean defaultValue) {
        return genericGet(key, NetworkTableValue.makeBoolean(defaultValue)).getBoolean();
    }

    /**
     * Gets a double array from NetworkTables.
     *
     * @param key          The key to find the value under.
     * @param defaultValue The value to return if the subscriber doesn't exist.
     *
     * @return The double array referenced by the key.
     */
    public static double[] get(String key, double[] defaultValue) {
        return genericGet(key, NetworkTableValue.makeDoubleArray(defaultValue)).getDoubleArray();
    }

    /**
     * Gets a double from NetworkTables.
     *
     * @param key          The key to find the value under.
     * @param defaultValue The value to return if the subscriber doesn't exist.
     *
     * @return The double referenced by the key.
     */
    public static double get(String key, double defaultValue) {
        return genericGet(key, NetworkTableValue.makeDouble(defaultValue)).getDouble();
    }

    /**
     * Gets a float array from NetworkTables.
     *
     * @param key          The key to find the value under.
     * @param defaultValue The value to return if the subscriber doesn't exist.
     *
     * @return The float array referenced by the key.
     */
    public static float[] get(String key, float[] defaultValue) {
        return genericGet(key, NetworkTableValue.makeFloatArray(defaultValue)).getFloatArray();
    }

    /**
     * Gets a float from NetworkTables.
     *
     * @param key          The key to find the value under.
     * @param defaultValue The value to return if the subscriber doesn't exist.
     *
     * @return The float referenced by the key.
     */
    public static float get(String key, float defaultValue) {
        return genericGet(key, NetworkTableValue.makeFloat(defaultValue)).getFloat();
    }

    /**
     * Gets an int array from NetworkTables.
     *
     * @param key          The key to find the value under.
     * @param defaultValue The value to return if the subscriber doesn't exist.
     *
     * @return The integer array referenced by the key.
     */
    public static int[] get(String key, int[] defaultValue) {
        // Converting the int[] to a long[]
        long[] newDefault = new long[defaultValue.length];
        for (int i = 0; i < defaultValue.length; i++) {
            newDefault[i] = defaultValue[i];
        }

        long[] subscriberLongs = genericGet(key, NetworkTableValue.makeIntegerArray(newDefault)).getIntegerArray();

        // Converting the subscriber output to an int array and limiting the min and max
        // values to the integer min and max
        int[] subscriberInts = new int[subscriberLongs.length];
        for (int i = 0; i < subscriberLongs.length; i++) {
            long subscriberLong = subscriberLongs[i];

            // Enforcing limits
            if (subscriberLong > Integer.MAX_VALUE)
                subscriberLong = Integer.MAX_VALUE;
            if (subscriberLong < Integer.MIN_VALUE)
                subscriberLong = Integer.MIN_VALUE;

            subscriberInts[i] = (int) subscriberLong;
        }

        return subscriberInts;
    }

    /**
     * Gets an int from NetworkTables.
     *
     * @param key          The key to find the value under.
     * @param defaultValue The value to return if the subscriber doesn't exist.
     *
     * @return The int referenced by the key.
     */
    public static int get(String key, int defaultValue) {
        long subscriberLong = genericGet(key, NetworkTableValue.makeInteger(defaultValue)).getInteger();

        // Converting the subscriber output to an int and limiting the min and max
        // values to the integer min and max.
        if (subscriberLong > Integer.MAX_VALUE)
            subscriberLong = Integer.MAX_VALUE;
        if (subscriberLong < Integer.MIN_VALUE)
            subscriberLong = Integer.MIN_VALUE;

        return (int) subscriberLong;
    }

    /**
     * Gets a string array from NetworkTables.
     *
     * @param key          The key to find the value under.
     * @param defaultValue The value to return if the subscriber doesn't exist.
     *
     * @return The string array referenced by the key.
     */
    public static String[] get(String key, String[] defaultValue) {
        return genericGet(key, NetworkTableValue.makeStringArray(defaultValue)).getStringArray();
    }

    /**
     * Gets a string from NetworkTables.
     *
     * @param key          The key to find the value under.
     * @param defaultValue The value to return if the subscriber doesn't exist.
     *
     * @return The string referenced by the key.
     */
    public static String get(String key, String defaultValue) {
        return genericGet(key, NetworkTableValue.makeString(defaultValue)).getString();
    }

    /**
     * Gets an array of struct serialized objects from NetworkTables.
     *
     * @param key          The key to find the value under.
     * @param defaultValue The value to return if the subscriber doesn't exist.
     * @param <T>          An object to log that implements
     *                     {@link StructSerializable}.
     *
     * @return The array of {@link StructSerializable} objects referenced by the
     *         key.
     */
    @SuppressWarnings("unchecked")
    public static <T extends StructSerializable> T[] get(String key,
            T[] defaultValue) {
        String ntPath = key;

        // Checking if the key is an alias or not
        if (aliasToNTPath.containsKey(key)) {
            ntPath = aliasToNTPath.get(key);
        }

        // Finding the struct for this StructSerializable object.
        Struct<T> struct = null;
        try {
            struct = (Struct<T>) defaultValue.getClass().getComponentType()
                    .getDeclaredField("struct").get(defaultValue);
        } catch (IllegalAccessException | NoSuchFieldException err) {
            DriverStation.reportError(
                    "No public instance of struct for the StructSerializable object "
                            + defaultValue.getClass().getName(),
                    err.getStackTrace());
            return defaultValue;
        }

        StructArrayTopic<T> topic = table.getStructArrayTopic(ntPath, struct);

        // Making sure the topic points to the right type of StructSerializable object.
        // This will be false if the key has already been used under a different name.
        if (!topic.getTypeString().equals(struct.getTypeString())) {
            subTypeMismatch(ntPath, "StructArray");
            return defaultValue;
        }

        // Updating the lastReads entry
        lastReads.put(key, System.currentTimeMillis());

        return topic.subscribe(defaultValue).get();
    }

    /**
     * Gets a struct serialized object from NetworkTables.
     *
     * @param key          The key to find the value under.
     * @param defaultValue The value to return if the subscriber doesn't exist.
     * @param <T>          An object to log that implements
     *                     {@link StructSerializable}.
     *
     * @return The struct serialized object referenced by the key.
     */
    @SuppressWarnings("unchecked")
    public static <T extends StructSerializable> T get(String key,
            T defaultValue) {
        String ntPath = key;

        // Checking if the key is an alias or not
        if (aliasToNTPath.containsKey(key)) {
            ntPath = aliasToNTPath.get(key);
        }

        // Finding the struct for this StructSerializable object.
        Struct<T> struct = null;

        try {
            struct = (Struct<T>) defaultValue.getClass()
                    .getDeclaredField("struct").get(defaultValue);
        } catch (IllegalAccessException | NoSuchFieldException err) {
            DriverStation.reportError(
                    "No public instance of struct for the StructSerializable object "
                            + defaultValue.getClass().getName(),
                    err.getStackTrace());
            return defaultValue;
        }

        StructTopic<T> topic = table.getStructTopic(ntPath, struct);

        // Making sure the topic points to the right type of StructSerializable object.
        // This will be false if the key has already been used under a different name.
        if (!topic.getTypeString().equals(struct.getTypeString())) {
            subTypeMismatch(ntPath, "Struct");
            return defaultValue;
        }

        // Updating the lastReads entry
        lastReads.put(key, System.currentTimeMillis());

        return topic.subscribe(defaultValue).get();
    }

    /**
     * Gets whether or not the logged value has changed since the last time the key
     * was read from.
     *
     * @param key The key to check the status of. This can be the path in
     *            NetworkTables or an alias.
     *
     * @return Whether or not the logged value has changed.
     */
    public static boolean hasChanged(String key) {
        String ntPath = getNTPathFromKey(key);

        // Checking if the ntPath has been published.
        if (lastReads.containsKey(key)) {
            // Comparing the time the value was last changed to the time it was last read.
            Topic topic = table.getTopic(ntPath);
            return lastReads.get(key) < topic.genericSubscribe()
                    .getLastChange();
        }

        return false;
    }

    /**
     * Removes a key from the logger.
     *
     * <p>
     * If the key is an alias, it removes the parent NetworkTables path and all
     * other aliases.
     *
     * @param key The key to remove. It can be an alias or a NetworkTables path.
     */
    public static void remove(String key) {
        String ntPath = getNTPathFromKey(key);

        // Removing the topic from NT
        Topic topic = table.getTopic(ntPath);
        table.removeListener(topic.getHandle());

        lastReads.remove(ntPath);

        // Removing all the aliases for the ntPath
        aliasToNTPath.entrySet()
                .removeIf(entry -> entry.getValue().equals(ntPath));
    }

    /**
     * Creates an alias for a key. Aliases are accepted as alternatives for the key
     * in the TurboLogger.log or TurboLogger.get methods. They can also increase
     * readability in the code.
     *
     * <p>
     * Aliases have their own entry in the lastRead table. This means that when you
     * get an alias, it does not mark the main key or any other aliases for that key
     * as read.
     *
     * @param ntPath The path to create an alias for.
     * @param alias  The alias to add.
     */
    public static void addAlias(String ntPath, String alias) {
        // Checking that the alias doesn't overlap with any existing keys.
        List<String> topics = table.getTopics().stream().map(Topic::getName).toList();
        if (topics.contains(alias)) {
            DriverStation.reportWarning("Alias \"" + alias
                    + "\" cannot be created because it overlaps with an existing NetworkTables key.",
                    false);
            return;
        }

        // Checking that the alias doesn't overlap with any existing aliases
        if (aliasToNTPath.containsKey(alias)) {
            DriverStation.reportWarning("Alias \"" + alias
                    + "\" cannot be created because it is already an alias for key \""
                    + aliasToNTPath.get(alias) + "\".", false);
            return;
        }

        // Recording the alias in the aliasToNTKey table.
        aliasToNTPath.put(alias, ntPath);

        // Adding the alias to the lastReads table
        lastReads.put(alias, 0l);
    }

    /**
     * Removes an alias.
     *
     * <p>
     * This does not remove the parent key or affect any of the other other aliases
     * associated with that key.
     *
     * @param alias The alias to remove.
     */
    public static void removeAlias(String alias) {
        // Making sure the parameter is an alias
        if (!aliasToNTPath.containsKey(alias))
            return;

        // Removing the alias from the maps
        aliasToNTPath.remove(alias);
        lastReads.remove(alias);
    }
}
