package cn.ryoii.baka.engine;

import cn.ryoii.baka.value.ByteString;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class HashIndexManager implements IndexManager {

    private final Map<ByteString, BakaHint> map = new ConcurrentHashMap<>();

    HashIndexManager() {}

    @Override
    public void put(BakaHint hint) {
        if (hint.valueSize == 0) {
            map.remove(hint.key);
        } else {
            map.put(hint.key, hint);
        }
    }

    @Override
    public BakaHint get(ByteString key) {
        return map.get(key);
    }

    @Override
    public Iterator<BakaHint> iterator() {
        return map.values().iterator();
    }
}
