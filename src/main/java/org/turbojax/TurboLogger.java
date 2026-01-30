package org.turbojax;

import edu.wpi.first.networktables.*;
import edu.wpi.first.util.struct.Struct;
import edu.wpi.first.util.struct.StructSerializable;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.DriverStation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TurboLogger {
    // Hashmaps for NT logging
    private static final HashMap<String, Long> lastReads = new HashMap<>();
    private static final HashMap<String, List<String>> ntPathToAliases = new HashMap<>();
    private static final HashMap<String, String> aliasToNTPath = new HashMap<>();

    private static final NetworkTableInstance instance = NetworkTableInstance.getDefault();
    private static final NetworkTable table = instance.getTable("TurboLogger");

    /**
     * Enables DataLog recording of NT output.
     *
     * <p>The wpilog is created at the logPath point. If the logPath ends with a forward slash, it
     * is seen as a directory. Otherwise, it is seen as a file.
     *
     * @param logPath The path to store the logfile at.
     */
    public static void enableDataLogs(String logPath) {
        // Checking if the path contains a file.
        // If there is a period in the path, it is considered to be a file.
        if (logPath.contains(".")) {
            // Splitting the path and putting it back together without the file name.
            StringBuilder path = new StringBuilder();
            String[] pathParts = logPath.split("[/\\\\]");
            for (int i = 0; i < pathParts.length - 1; i++) {
                path.append(pathParts[i]).append("/");
            }

            // Starting the logger at the path and file location.
            DataLogManager.start(path.toString(), pathParts[pathParts.length - 1]);
        } else {
            DataLogManager.start(logPath, "");
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
     * Reports when a publisher or subscriber have inconsistent types with what is being logged or read.
     *
     * @param key   The key being logged to or read.
     * @param type  The class being logged.
     * @param isPub Whether or not the issue occurred with a publisher.
     */
    private static void pubsubTypeMismatch(String key, String type, boolean isPub) {
        String pubsub = isPub ? "Publisher" : "Subscriber";
        if (aliasToNTPath.containsKey(key)) {
            DriverStation.reportWarning(pubsub + " is not an instance of " + type + pubsub + " for alias \"" + key + "\" of key \"" + aliasToNTPath.get(key) + "\".", false);
        } else {
            DriverStation.reportWarning(pubsub + " is not an instance of " + type + pubsub + " for key \"" + key + "\".", false);
        }
    }

    // TODO: Should I use this method or stick with the other method above?
    /**
     * Reports when the internal type of logged or read {@link StructSerializable} objects do not match what type is being requested.
     *
     * @param key         The key being read.
     * @param desiredType The class the logger is attempting to read.
     * @param realType    The class of the variable in NetworkTables.
     */
    private static void structTypeMismatch(String key, String desiredType, String realType) {
        String warning = "";
        if (aliasToNTPath.containsKey(key)) {
            warning = String.format("The object at alias \"%s\" of key \"%s\" is not an instance of %s. It is a %s.", key, aliasToNTPath.get(key), desiredType, realType);
        } else {
            warning = String.format("The object at key \"%s\" is not an instance of %s.  It is a %s.", key, desiredType, realType);
        }

        DriverStation.reportWarning(warning, false);
    }

    // Loggers

    /**
     * Logs a boolean array to NetworkTables.
     *
     * <p>It also creates any of the aliases passed into the array.
     *
     * @param key     The key to log the value under. This can be a NetworkTables path or an alias.
     * @param value   The boolean array to log.
     * @param aliases Any aliases to add to the ntPath.
     */
    public static void log(String key, boolean[] value, String... aliases) {
        String ntPath = key;  

        // Checking if the key is an alias
        if (aliasToNTPath.containsKey(key)) {

        }

        // Getting the BooleanArrayTopic
        BooleanArrayTopic topic = table.getBooleanArrayTopic(key);

        // Making sure the topic points to a boolean array.
        // This will be false if the key has already been used under a different name.
        if (topic.getType() != NetworkTableType.kBooleanArray) {
            pubsubTypeMismatch(key, "BooleanArray", false);
            return;
        }

        // Pushing the value
        topic.publish().set(value);

        // Saving any aliases for the key
        if (aliases.length > 0) addAliases(key, aliases);

        // Resetting the lastRead entry for the key
        lastReads.put(key, 0L);

        // Updating the lastRead entry for all the aliases of this key.
        for (String alias : ntPathToAliases.getOrDefault(key, new ArrayList<>())) {
            lastReads.put(alias, 0L);
        }
    }

    /**
     * Logs a boolean to NetworkTables.
     *
     * <p>It also creates any of the aliases passed into the array.
     *
     * @param key     The key to log the value under. This can be a NetworkTables path or an alias.
     * @param value   The boolean to log.
     * @param aliases Any aliases to add to the ntPath.
     */
    public static void log(String key, boolean value, String... aliases) {
        // Getting the BooleanTopic
        BooleanTopic topic = table.getBooleanTopic(key);

        // Making sure the topic points to a boolean.
        // This will be false if the key has already been used under a different name.
        if (topic.getType() != NetworkTableType.kBoolean) {
            pubsubTypeMismatch(key, "Boolean", false);
            return;
        }

        // Pushing the value
        topic.publish().set(value);

        // Saving any aliases for the key
        if (aliases.length > 0) addAliases(key, aliases);

        // Resetting the lastRead entry for the key
        lastReads.put(key, 0L);

        // Updating the lastRead entry for all the aliases of this key.
        for (String alias : ntPathToAliases.getOrDefault(key, new ArrayList<>())) {
            lastReads.put(alias, 0L);
        }
    }

    /**
     * Logs a double array to NetworkTables.
     *
     * <p>It also creates any of the aliases passed into the array.
     *
     * @param key     The key to log the value under. This can be a NetworkTables path or an alias.
     * @param value   The double array to log.
     * @param aliases Any aliases to add to the ntPath.
     */
    public static void log(String key, double[] value, String... aliases) {
        // Getting the DoubleArrayTopic
        DoubleArrayTopic topic = table.getDoubleArrayTopic(key);

        // Making sure the topic points to a double array.
        // This will be false if the key has already been used under a different name.
        if (topic.getType() != NetworkTableType.kDoubleArray) {
            pubsubTypeMismatch(key, "DoubleArray", false);
            return;
        }

        // Pushing the value
        topic.publish().set(value);

        // Saving any aliases for the key
        if (aliases.length > 0) addAliases(key, aliases);

        // Resetting the lastRead entry for the key
        lastReads.put(key, 0L);

        // Updating the lastRead entry for all the aliases of this key.
        for (String alias : ntPathToAliases.getOrDefault(key, new ArrayList<>())) {
            lastReads.put(alias, 0L);
        }
    }

    /**
     * Logs a double to NetworkTables.
     *
     * <p>It also creates any of the aliases passed into the array.
     *
     * @param key     The key to log the value under. This can be a NetworkTables path or an alias.
     * @param value   The double to log.
     * @param aliases Any aliases to add to the ntPath.
     */
    public static void log(String key, double value, String... aliases) {
        // Getting the DoubleTopic
        DoubleTopic topic = table.getDoubleTopic(key);

        // Making sure the topic points to a double.
        // This will be false if the key has already been used under a different name.
        if (topic.getType() != NetworkTableType.kDouble) {
            pubsubTypeMismatch(key, "Double", false);
            return;
        }

        // Pushing the value
        topic.publish().set(value);

        // Saving any aliases for the key
        if (aliases.length > 0) addAliases(key, aliases);

        // Resetting the lastRead entry for the key
        lastReads.put(key, 0L);

        // Updating the lastRead entry for all the aliases of this key.
        for (String alias : ntPathToAliases.getOrDefault(key, new ArrayList<>())) {
            lastReads.put(alias, 0L);
        }
    }

    /**
     * Logs a float array to NetworkTables.
     *
     * <p>It also creates any of the aliases passed into the array.
     *
     * @param key     The key to log the value under. This can be a NetworkTables path or an alias.
     * @param value   The float array to log.
     * @param aliases Any aliases to add to the ntPath.
     */
    public static void log(String key, float[] value, String... aliases) {
        // Getting the FloatArrayTopic
        FloatArrayTopic topic = table.getFloatArrayTopic(key);

        // Making sure the topic points to a float array.
        // This will be false if the key has already been used under a different name.
        if (topic.getType() != NetworkTableType.kFloatArray) {
            pubsubTypeMismatch(key, "FloatArray", false);
            return;
        }

        // Pushing the value
        topic.publish().set(value);

        // Saving any aliases for the key
        if (aliases.length > 0) addAliases(key, aliases);

        // Resetting the lastRead entry for the key
        lastReads.put(key, 0L);

        // Updating the lastRead entry for all the aliases of this key.
        for (String alias : ntPathToAliases.getOrDefault(key, new ArrayList<>())) {
            lastReads.put(alias, 0L);
        }
    }

    /**
     * Logs a float to NetworkTables.
     *
     * <p>It also creates any of the aliases passed into the array.
     *
     * @param key     The key to log the value under. This can be a NetworkTables path or an alias.
     * @param value   The float to log.
     * @param aliases Any aliases to add to the ntPath.
     */
    public static void log(String key, float value, String... aliases) {
        // Getting the FloatTopic
        FloatTopic topic = table.getFloatTopic(key);

        // Making sure the topic points to a float.
        // This will be false if the key has already been used under a different name.
        if (topic.getType() != NetworkTableType.kFloat) {
            pubsubTypeMismatch(key, "Float", false);
            return;
        }

        // Pushing the value
        topic.publish().set(value);

        // Saving any aliases for the key
        if (aliases.length > 0) addAliases(key, aliases);

        // Resetting the lastRead entry for the key
        lastReads.put(key, 0L);

        // Updating the lastRead entry for all the aliases of this key.
        for (String alias : ntPathToAliases.getOrDefault(key, new ArrayList<>())) {
            lastReads.put(alias, 0L);
        }
    }

    /**
     * Logs an int array to NetworkTables.
     *
     * <p>It also creates any of the aliases passed into the array.
     *
     * @param key     The key to log the value under. This can be a NetworkTables path or an alias.
     * @param value   The int array to log.
     * @param aliases Any aliases to add to the ntPath.
     */
    public static void log(String key, int[] value, String... aliases) {
        // Getting the IntegerArrayTopic
        IntegerArrayTopic topic = table.getIntegerArrayTopic(key);

        // Making sure the topic points to an integer array.
        // This will be false if the key has already been used under a different name.
        if (topic.getType() != NetworkTableType.kIntegerArray) {
            pubsubTypeMismatch(key, "IntegerArray", false);
            return;
        }

        // Converting the int array to a long array
        long[] new_value = new long[value.length];
        for (int i = 0; i < value.length; i++) {
            new_value[i] = value[i];
        }

        // Pushing the value
        topic.publish().set(new_value);

        // Saving any aliases for the key
        if (aliases.length > 0) addAliases(key, aliases);

        // Resetting the lastRead entry for the key
        lastReads.put(key, 0L);

        // Updating the lastRead entry for all the aliases of this key.
        for (String alias : ntPathToAliases.getOrDefault(key, new ArrayList<>())) {
            lastReads.put(alias, 0L);
        }
    }

    /**
     * Logs an int to NetworkTables.
     *
     * <p>It also creates any of the aliases passed into the array.
     *
     * @param key     The key to log the value under. This can be a NetworkTables path or an alias.
     * @param value   The int to log.
     * @param aliases Any aliases to add to the ntPath.
     */
    public static void log(String key, int value, String... aliases) {
        // Getting the IntegerTopic
        IntegerTopic topic = table.getIntegerTopic(key);

        // Making sure the topic points to an integer.
        // This will be false if the key has already been used under a different name.
        if (topic.getType() != NetworkTableType.kInteger) {
            pubsubTypeMismatch(key, "Integer", false);
            return;
        }

        // Pushing the value
        topic.publish().set(value);

        // Saving any aliases for the key
        if (aliases.length > 0) addAliases(key, aliases);

        // Resetting the lastRead entry for the key
        lastReads.put(key, 0L);

        // Updating the lastRead entry for all the aliases of this key.
        for (String alias : ntPathToAliases.getOrDefault(key, new ArrayList<>())) {
            lastReads.put(alias, 0L);
        }
    }

    /**
     * Logs a string array to NetworkTables.
     *
     * <p>It also creates any of the aliases passed into the array.
     *
     * @param key     The key to log the value under. This can be a NetworkTables path or an alias.
     * @param value   The string array to log.
     * @param aliases Any aliases to add to the ntPath.
     */
    public static void log(String key, String[] value, String... aliases) {
        // Getting the StringArrayTopic
        StringArrayTopic topic = table.getStringArrayTopic(key);

        // Making sure the topic points to a string array.
        // This will be false if the key has already been used under a different name.
        if (topic.getType() != NetworkTableType.kStringArray) {
            pubsubTypeMismatch(key, "StringArray", false);
            return;
        }

        // Pushing the value
        topic.publish().set(value);

        // Saving any aliases for the key
        if (aliases.length > 0) addAliases(key, aliases);

        // Resetting the lastRead entry for the key
        lastReads.put(key, 0L);

        // Updating the lastRead entry for all the aliases of this key.
        for (String alias : ntPathToAliases.getOrDefault(key, new ArrayList<>())) {
            lastReads.put(alias, 0L);
        }
    }

    /**
     * Logs a string to NetworkTables.
     *
     * <p>It also creates any of the aliases passed into the array.
     *
     * @param key     The key to log the value under. This can be a NetworkTables path or an alias.
     * @param value   The string to log.
     * @param aliases Any aliases to add to the ntPath.
     */
    public static void log(String key, String value, String... aliases) {
        // Getting the StringTopic
        StringTopic topic = table.getStringTopic(key);

        // Making sure the topic points to a string.
        // This will be false if the key has already been used under a different name.
        if (topic.getType() != NetworkTableType.kString) {
            pubsubTypeMismatch(key, "String", false);
            return;
        }

        // Pushing the value
        topic.publish().set(value);

        // Saving any aliases for the key
        if (aliases.length > 0) addAliases(key, aliases);

        // Resetting the lastRead entry for the key
        lastReads.put(key, 0L);

        // Updating the lastRead entry for all the aliases of this key.
        for (String alias : ntPathToAliases.getOrDefault(key, new ArrayList<>())) {
            lastReads.put(alias, 0L);
        }
    }

    /**
     * Logs a struct array to NetworkTables.
     *
     * <p>It also creates any of the aliases passed into the array.
     *
     * @param key     The key to log the value under. This can be a NetworkTables path or an alias.
     * @param value   The struct array to log.
     * @param aliases Any aliases to add to the ntPath.
     * @param <T>     An object to log that implements {@link StructSerializable}.
     */
    @SuppressWarnings("unchecked")
    public static <T extends StructSerializable> void log(String key, T[] value, String... aliases) {
        // Finding the struct for this StructSerializable object.
        Struct<T> struct = null;
        try {
            struct = (Struct<T>) value.getClass().getComponentType().getDeclaredField("struct").get(value);
        } catch (IllegalAccessException | NoSuchFieldException err) {
            DriverStation.reportError("No public instance of struct for the StructSerializable object " + value.getClass().getName(), err.getStackTrace());
            return;
        }

        // Getting the StructArrayTopic
        StructArrayTopic<T> topic = table.getStructArrayTopic(key, struct);

        // Making sure the topic points to the right type of StructSerializable object.
        // This will be false if the key has already been used under a different name.
        if (!topic.getTypeString().equals(struct.getTypeString())) {
            pubsubTypeMismatch(key, "StructArray", false);
            return;
        }

        // Pushing the value
        topic.publish().set(value);

        // Saving any aliases for the key
        if (aliases.length > 0) addAliases(key, aliases);

        // Resetting the lastRead entry for the key
        lastReads.put(key, 0L);

        // Updating the lastRead entry for all the aliases of this key.
        for (String alias : ntPathToAliases.getOrDefault(key, new ArrayList<>())) {
            lastReads.put(alias, 0L);
        }
    }

    /**
     * Logs a struct to NetworkTables.
     *
     * <p>It also creates any of the aliases passed into the array.
     *
     * @param key     The key to log the value under. This can be a NetworkTables path or an alias.
     * @param value   The struct to log.
     * @param aliases Any aliases to add to the ntPath.
     * @param <T>     An object to log that implements {@link StructSerializable}.
     */
    @SuppressWarnings("unchecked")
    public static <T extends StructSerializable> void log(String key, T value, String... aliases) {
        // Finding the struct for this StructSerializable object.
        Struct<T> struct = null;

        try {
            struct = (Struct<T>) value.getClass().getDeclaredField("struct").get(value);
        } catch (IllegalAccessException | NoSuchFieldException err) {
            DriverStation.reportError("No public instance of struct for the StructSerializable object " + value.getClass().getName(), err.getStackTrace());
            return;
        }

        // Getting the StructTopic
        StructTopic<T> topic = table.getStructTopic(key, struct);

        // Making sure the topic points to the right type of StructSerializable object.
        // This will be false if the key has already been used under a different name.
        if (!topic.getTypeString().equals(struct.getTypeString())) {
            pubsubTypeMismatch(key, "Struct", false);
            return;
        }

        // Pushing the value
        topic.publish().set(value);

        // Saving any aliases for the key
        if (aliases.length > 0) addAliases(key, aliases);

        // Resetting the lastRead entry for the key
        lastReads.put(key, 0L);

        // Updating the lastRead entry for all the aliases of this key.
        for (String alias : ntPathToAliases.getOrDefault(key, new ArrayList<>())) {
            lastReads.put(alias, 0L);
        }
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
        BooleanArrayTopic topic = table.getBooleanArrayTopic(key);

        // Making sure the topic points to a boolean array.
        // This will be false if the key has already been used under a different name.
        if (topic.getType() != NetworkTableType.kBooleanArray) {
            pubsubTypeMismatch(key, "BooleanArray", false);
            return defaultValue;
        }

        return topic.subscribe(defaultValue).get();
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
        BooleanTopic topic = table.getBooleanTopic(key);

        // Making sure the topic points to a boolean.
        // This will be false if the key has already been used under a different name.
        if (topic.getType() != NetworkTableType.kBoolean) {
            pubsubTypeMismatch(key, "Boolean", false);
            return defaultValue;
        }

        return topic.subscribe(defaultValue).get();
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
        DoubleArrayTopic topic = table.getDoubleArrayTopic(key);

        // Making sure the topic points to a double array.
        // This will be false if the key has already been used under a different name.
        if (topic.getType() != NetworkTableType.kDoubleArray) {
            pubsubTypeMismatch(key, "DoubleArray", false);
            return defaultValue;
        }

        return topic.subscribe(defaultValue).get();
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
        DoubleTopic topic = table.getDoubleTopic(key);

        // Making sure the topic points to a double.
        // This will be false if the key has already been used under a different name.
        if (topic.getType() != NetworkTableType.kDouble) {
            pubsubTypeMismatch(key, "Double", false);
            return defaultValue;
        }

        return topic.subscribe(defaultValue).get();
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
        FloatArrayTopic topic = table.getFloatArrayTopic(key);

        // Making sure the topic points to a float array.
        // This will be false if the key has already been used under a different name.
        if (topic.getType() != NetworkTableType.kFloatArray) {
            pubsubTypeMismatch(key, "FloatArray", false);
            return defaultValue;
        }

        return topic.subscribe(defaultValue).get();
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
        FloatTopic topic = table.getFloatTopic(key);

        // Making sure the topic points to a float.
        // This will be false if the key has already been used under a different name.
        if (topic.getType() != NetworkTableType.kFloat) {
            pubsubTypeMismatch(key, "Float", false);
            return defaultValue;
        }

        return topic.subscribe(defaultValue).get();
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
        IntegerArrayTopic topic = table.getIntegerArrayTopic(key);

        // Making sure the topic points to an int array.
        // This will be false if the key has already been used under a different name.
        if (topic.getType() != NetworkTableType.kIntegerArray) {
            pubsubTypeMismatch(key, "IntegerArray", false);
            return defaultValue;
        }

        // Converting the int[] to a long[]
        long[] newDefault = new long[defaultValue.length];
        for (int i = 0; i < defaultValue.length; i++) {
            newDefault[i] = defaultValue[i];
        }

        long[] subscriberLongs = topic.subscribe(newDefault).get();
        
        // Converting the subscriber output to an int array and limiting the min and max values to the integer min and max
        int[] subscriberInts = new int[subscriberLongs.length];
        for (int i = 0; i < subscriberLongs.length; i++) {
            long subscriberLong = subscriberLongs[i];

            // Enforcing limits
            if (subscriberLong > Integer.MAX_VALUE) subscriberLong = Integer.MAX_VALUE;
            if (subscriberLong < Integer.MIN_VALUE) subscriberLong = Integer.MIN_VALUE;

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
        IntegerTopic topic = table.getIntegerTopic(key);

        // Making sure the topic points to an int.
        // This will be false if the key has already been used under a different name.
        if (topic.getType() != NetworkTableType.kInteger) {
            pubsubTypeMismatch(key, "Integer", false);
            return defaultValue;
        }

        long subscriberLong = topic.subscribe(defaultValue).get();
        
        // Converting the subscriber output to an int and limiting the min and max values to the integer min and max.
        if (subscriberLong > Integer.MAX_VALUE) subscriberLong = Integer.MAX_VALUE;
        if (subscriberLong < Integer.MIN_VALUE) subscriberLong = Integer.MIN_VALUE;

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
        StringArrayTopic topic = table.getStringArrayTopic(key);

        // Making sure the topic points to a string array.
        // This will be false if the key has already been used under a different name.
        if (topic.getType() != NetworkTableType.kStringArray) {
            pubsubTypeMismatch(key, "StringArray", false);
            return defaultValue;
        }

        return topic.subscribe(defaultValue).get();
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
        StringTopic topic = table.getStringTopic(key);

        // Making sure the topic points to a string.
        // This will be false if the key has already been used under a different name.
        if (topic.getType() != NetworkTableType.kString) {
            pubsubTypeMismatch(key, "String", false);
            return defaultValue;
        }

        return topic.subscribe(defaultValue).get();
    }

    /**
     * Gets an array of struct serialized objects from NetworkTables.
     *
     * @param key          The key to find the value under.
     * @param defaultValue The value to return if the subscriber doesn't exist.
     * @param <T>          An object to log that implements {@link StructSerializable}.
     *
     * @return The array of {@link StructSerializable} objects referenced by the key.
     */
    @SuppressWarnings("unchecked")
    public static <T extends StructSerializable> T[] get(String key, T[] defaultValue) {
        // Finding the struct for this StructSerializable object.
        Struct<T> struct = null;
        try {
            struct = (Struct<T>) defaultValue.getClass().getComponentType().getDeclaredField("struct").get(defaultValue);
        } catch (IllegalAccessException | NoSuchFieldException err) {
            DriverStation.reportError("No public instance of struct for the StructSerializable object " + defaultValue.getClass().getName(), err.getStackTrace());
            return defaultValue;
        }

        StructArrayTopic<T> topic = table.getStructArrayTopic(key, struct);

        // Making sure the topic points to the right type of StructSerializable object.
        // This will be false if the key has already been used under a different name.
        if (!topic.getTypeString().equals(struct.getTypeString())) {
            pubsubTypeMismatch(key, "StructArray", false);
            return defaultValue;
        }

        return topic.subscribe(defaultValue).get();
    }

    /**
     * Gets a struct serialized object from NetworkTables.
     *
     * @param key          The key to find the value under.
     * @param defaultValue The value to return if the subscriber doesn't exist.
     * @param <T>          An object to log that implements {@link StructSerializable}.
     *
     * @return The struct serialized object referenced by the key.
     */
    @SuppressWarnings("unchecked")
    public static <T extends StructSerializable> T get(String key, T defaultValue) {
        // Finding the struct for this StructSerializable object.
        Struct<T> struct = null;

        try {
            struct = (Struct<T>) defaultValue.getClass().getDeclaredField("struct").get(defaultValue);
        } catch (IllegalAccessException | NoSuchFieldException err) {
            DriverStation.reportError("No public instance of struct for the StructSerializable object " + defaultValue.getClass().getName(), err.getStackTrace());
            return defaultValue;
        }

        StructTopic<T> topic = table.getStructTopic(key, struct);

        // Making sure the topic points to the right type of StructSerializable object.
        // This will be false if the key has already been used under a different name.
        if (!topic.getTypeString().equals(struct.getTypeString())) {
            pubsubTypeMismatch(key, "Struct", false);
            return defaultValue;
        }

        return topic.subscribe(defaultValue).get();
    }

    /**
     * Adds aliases to a key. Aliases are accepted as alternatives for the key in the TurboLogger.log
     * or TurboLogger.get methods.  They can also increase readability in the code.
     *
     * <p>Aliases have their own entry in the lastRead table.  This means that when you get an alias, it
     * does not mark the main key or any other aliases for that key as read.
     *
     * @param ntPath  The path to create an alias for.
     * @param aliases The aliases to add.
     */
    public static void addAliases(String ntPath, String... aliases) {
        // This check isn't necessary, but I want it to report a warning if someone tried it.
        if (aliases.length == 0) {
            DriverStation.reportWarning("Please don't use addAliases with no alias parameters", false);
            return;
        }

        for (String alias : aliases) {
            // Skipping if the alias is the same as the path.
            table.getTopics().stream().map(Topic::getName);
            if (alias.equals(ntPath)) {
                DriverStation.reportWarning("Alias cannot have the same name as the NT path.  Skipping creation", false);
                continue;
            }

            // If the alias has already been assigned, it reports an error and doesn't add an entry
            // for this ntPath.
            if (aliasToNTPath.containsKey(alias)) {
                DriverStation.reportWarning("Alias \"" + alias + "\" has already been assigned to key \"" + aliasToNTPath.get(alias) + "\".  Skipping creation", false);
                continue;
            }

            // Recording the alias in the aliasToNTKey table.
            aliasToNTPath.put(alias, ntPath);

            // Adding the alias to the ntPathToAliases table.
            // If the list doesn't exist yet, it creates one.
            if (!ntPathToAliases.containsKey(ntPath)) {
                ntPathToAliases.put(ntPath, new ArrayList<String>());
            }

            ntPathToAliases.get(ntPath).add(alias);

            lastReads.put(alias, lastReads.get(ntPath));
        }
    }

    /**
     * Gets whether or not the logged value has changed since the last time the key was read from.
     *
     * @param key The key to check the status of. This can be the path in NetworkTables or an alias.
     *
     * @return Whether or not the logged value has changed.
     */
    public static boolean hasChanged(String key) {
        String ntPath = key;

        // Checking if the key is an alias
        if (aliasToNTPath.containsKey(key)) {
            ntPath = aliasToNTPath.get(key);
        }

        // Checking if the ntPath has been published.
        if (lastReads.containsKey(ntPath)) {
            // Comparing the time the value was last changed to the time it was last read.
            Topic topic = table.getTopic(ntPath);
            return lastReads.get(ntPath) < topic.genericSubscribe().getLastChange();
        }

        return false;
    }

    /**
     * Removes a key from the logger.
     *
     * <p>If the key is an alias, it removes the original value and all other aliases
     *
     * @param key The key to remove.  It can be an alias or a NetworkTables path.
     */
    public static void remove(String key) {
        String ntPath = key;

        // Checking if the key is an alias
        if (aliasToNTPath.containsKey(key)) {
            ntPath = aliasToNTPath.get(key);
        }

        // Removing the topic from NT
        Topic topic = table.getTopic(ntPath);
        table.removeListener(topic.getHandle());

        lastReads.remove(ntPath);

        // Removing all the aliases for the ntPath
        ntPathToAliases.remove(ntPath).forEach(alias -> aliasToNTPath.remove(alias));
    }

    public static void removeAlias(String alias) {
        // Making sure the parameter is an alias
        if (!aliasToNTPath.containsKey(alias)) return;

        // Removing the alias from the maps
        String ntPath = aliasToNTPath.remove(alias);

        List<String> aliases = ntPathToAliases.get(ntPath);
        aliases.remove(alias);
        ntPathToAliases.put(ntPath, aliases);
    }    
}
