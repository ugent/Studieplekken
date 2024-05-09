package blok2.model.generator;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import java.io.Serializable;
import java.lang.reflect.Field;

public class NullAwareGenerator extends SequenceStyleGenerator {
    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        try {
            Field idField = object.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Serializable id = (Serializable) idField.get(object);

            if (id != null) {
                return id;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return super.generate(session, object);
        }
        return super.generate(session, object);
    }
}

