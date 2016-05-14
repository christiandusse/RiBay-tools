package com.ribay.tools.tool;

import com.basho.riak.client.api.commands.kv.UpdateValue;
import com.ribay.tools.data.Article;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by CD on 14.05.2016.
 */
@Component
public class FixEncoding extends UpdateValue.Update<Article> implements CommandMarker {

    @Autowired
    private JobUtil util;

    @CliCommand(value = "fixEncoding", help = "Fixes encoding")
    public void start(@CliOption(key = {"bucket"}, mandatory = true, help = "The name of the bucket") final String bucket, //
                      @CliOption(key = {"start"}, mandatory = true, help = "The index where to start") final int idxStart, //
                      @CliOption(key = {"end"}, mandatory = true, help = "The index where to end") final int idxEnd) throws Exception {

        util.updateOnAll(bucket, idxStart, idxEnd, this);
    }

    @Override
    public Article apply(Article original) {
        try {
            boolean changed = fixEncoding(original);
            setModified(changed);
            return original;
        } catch (Exception e) {
            e.printStackTrace();
            setModified(false);
            return original;
        }
    }

    private static final LinkedHashMap<String, String> fixes = new LinkedHashMap<>();

    static {
        fixes.put("ÃŸ", "ß");
        fixes.put("Ã¶", "ö");
        fixes.put("Ã¼", "ü");
        fixes.put("Ã¤", "ä");
        fixes.put("Ã¨", "è");
        fixes.put("Ã©", "é");
        fixes.put("Ã¸", "ø");
        fixes.put("Ã±", "ñ");
        fixes.put("Ã¹", "ù");
        // TODO: add more replacements
    }

    private boolean fixEncoding(Object o) throws Exception {
        boolean changed = false;
        if (o != null) {
            Class<?> clazz = o.getClass();
            List<Field> fields = new ArrayList<>();
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            // TODO: add fields of super class?

            for (Field field : fields) {
                Class<?> fieldType = field.getType();
                if (fieldType.equals(String.class)) { // change string directly
                    field.setAccessible(true);
                    String fieldValue = (String) field.get(o);
                    if (fieldValue != null) {
                        String newFieldValue = fixEncodingString(fieldValue);

                        if (!fieldValue.equals(newFieldValue)) {
                            field.set(o, newFieldValue);
                            changed = true;
                        }
                    }
                } else if (Collection.class.isAssignableFrom(fieldType)) {
                    field.setAccessible(true);
                    Collection fieldValue = (Collection) field.get(o);
                    if (fieldValue != null) {
                        for (Object collectionEntry : new ArrayList(fieldValue)) { //copy of collection to prevent ConcurrentModificationException
                            if (collectionEntry instanceof String) { // list of strings: replace strings in collection
                                String stringInCollection = (String) collectionEntry;
                                if (stringInCollection != null) {
                                    String fixedString = fixEncodingString(stringInCollection);
                                    if (!fixedString.equals(stringInCollection)) {
                                        fieldValue.remove(stringInCollection);
                                        fieldValue.add(fixedString);
                                        changed = true;
                                    }
                                }
                            } else if (collectionEntry != null) {
                                Class<?> entryClazz = collectionEntry.getClass();
                                if (isAppDomainType(entryClazz)) { // list of domain classes: fix encoding recursive
                                    changed = changed || fixEncoding(collectionEntry);
                                }
                            }
                        }
                    }
                } else if (isAppDomainType(fieldType)) { // field is domain class: fix encoding recursive
                    field.setAccessible(true);
                    Object fieldValue = field.get(o);
                    if (fieldValue != null) {
                        changed = changed || fixEncoding(fieldValue);
                    }
                }
            }
        }
        return changed;
    }

    private boolean isAppDomainType(Class<?> clazz) {
        Package pack = clazz.getPackage();
        return (pack != null) && pack.getName().startsWith("com.ribay");
    }

    private String fixEncodingString(String s) throws Exception {
        for (Map.Entry<String, String> replacement : fixes.entrySet()) {
            s = s.replace(replacement.getKey(), replacement.getValue());
        }
        return s;
    }

}
