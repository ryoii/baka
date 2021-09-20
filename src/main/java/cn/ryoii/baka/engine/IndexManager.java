package cn.ryoii.baka.engine;

import cn.ryoii.baka.value.ByteString;

import java.util.Iterator;

interface IndexManager extends Iterable<BakaHint> {

    void put(BakaHint hint);

    BakaHint get(ByteString key);

    Iterator<BakaHint> iterator();
}
