/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.util.helper;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;

public class ReflectionLookup<T> {

    private final Map<Class, Field> cachedFields = new IdentityHashMap<Class, Field>();
    private final Class<T> typeClass;

    public ReflectionLookup(Class<T> typeClass) {
        this.typeClass = typeClass;
    }

    private Field findFieldInClass(Class clazz) {
        Field field = this.cachedFields.get(clazz);
        if (field != null) return field;

        for (Field foundField : clazz.getDeclaredFields()) {
            if (this.typeClass.isAssignableFrom(foundField.getType())) {
                field = foundField;
                break;
            }
        }
        if (field == null) return null;
        field.setAccessible(true);
        this.cachedFields.put(clazz, field);
        return field;
    }

    public T getObjectFrom(Object clazz) {
        Field field = this.findFieldInClass(clazz.getClass());
        if (field != null) {
            try {
                return this.typeClass.cast(field.get(clazz));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
