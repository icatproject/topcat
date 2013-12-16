package uk.ac.stfc.topcat.debug;

import java.util.Arrays;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * ToStringStyle taken from http://stackoverflow.com/a/20407041
 *
 * @Vadzim
 */

@SuppressWarnings("serial")
public class RecursiveToStringStyle extends ToStringStyle {

    private static final RecursiveToStringStyle INSTANCE = new RecursiveToStringStyle(13);

    public static ToStringStyle getInstance() {
        return INSTANCE;
    }

    public static String toString(Object value) {
        final StringBuffer sb = new StringBuffer(512);
        INSTANCE.appendDetail(sb, null, value);
        return sb.toString();
    }

    private final int maxDepth;
    private final char[] tabs;

    // http://stackoverflow.com/a/16934373/603516
    private ThreadLocal<MutableInteger> depth = new ThreadLocal<MutableInteger>() {
        @Override
        protected MutableInteger initialValue() {
            return new MutableInteger(0);
        }
    };

    public RecursiveToStringStyle(int maxDepth) {
        this.maxDepth = maxDepth;
        tabs = new char[maxDepth];
        Arrays.fill(tabs, '\t');

        setUseShortClassName(true);
        setUseIdentityHashCode(false);
        setContentStart(" {");
        setFieldSeparator(SystemUtils.LINE_SEPARATOR);
        setFieldSeparatorAtStart(true);
        setFieldNameValueSeparator(" = ");
        setContentEnd("}");
    }

    private int getDepth() {
        return depth.get().get();
    }

    private void padDepth(StringBuffer buffer) {
        buffer.append(tabs, 0, getDepth());
    }

    @Override
    protected void appendFieldSeparator(StringBuffer buffer) {
        buffer.append(getFieldSeparator());
        padDepth(buffer);
    }

    @Override
    public void appendStart(StringBuffer buffer, Object object) {
        depth.get().increment();
        super.appendStart(buffer, object);
    }

    @Override
    public void appendEnd(StringBuffer buffer, Object object) {
        super.appendEnd(buffer, object);
        buffer.setLength(buffer.length() - getContentEnd().length());
        buffer.append(SystemUtils.LINE_SEPARATOR);
        depth.get().decrement();
        padDepth(buffer);
        appendContentEnd(buffer);
    }

    @Override
    protected void removeLastFieldSeparator(StringBuffer buffer) {
        int len = buffer.length();
        int sepLen = getFieldSeparator().length() + getDepth();
        if (len > 0 && sepLen > 0 && len >= sepLen) {
            buffer.setLength(len - sepLen);
        }
    }

    private boolean noReflectionNeeded(Object value) {
        try {
            return value != null &&
                    (value.getClass().getName().startsWith("java.lang.")
                    || value.getClass().getMethod("toString").getDeclaringClass() != Object.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected void appendDetail(StringBuffer buffer, String fieldName, Object value) {
        if (getDepth() >= maxDepth || noReflectionNeeded(value)) {
            buffer.append(value);
        } else {
            new ReflectionToStringBuilder(value, this, buffer, null, false, false).toString();
        }
    }

    static class MutableInteger {
        private int value;

        MutableInteger(int value) {
            this.value = value;
        }

        public final int get() {
            return value;
        }

        public final void increment() {
            ++value;
        }

        public final void decrement() {
            --value;
        }
    }
}
