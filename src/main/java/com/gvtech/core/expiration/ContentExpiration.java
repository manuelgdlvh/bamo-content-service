package com.gvtech.core.expiration;

import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import lombok.Getter;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

@Getter
public class ContentExpiration implements Delayed {
    private final ContentId id;
    private final ContentType type;
    private final Long expirationInMillis;

    public ContentExpiration(final ContentId id, ContentType type, final Long expirationInMillis) {
        this.id = id;
        this.type = type;
        this.expirationInMillis = System.currentTimeMillis() + expirationInMillis;

    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ContentExpiration other))
            return false;
        return this.id.equals(other.getId()) && this.type.equals(other.type);
    }

    @Override
    public final int hashCode() {
        int result = 17;
        if (id != null) {
            result = 31 * result + id.hashCode();
            result = 31 * result + type.hashCode();

        }
        return result;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = expirationInMillis - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return this.expirationInMillis.compareTo(((ContentExpiration) o).expirationInMillis);
    }
}
