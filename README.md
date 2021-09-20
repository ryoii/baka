<h1 align="center">Baka Db</h1>

An embedded database implemented in pure java based on bitcask which is a log-structured hash table for K/V Data.

### Usage

```java
import cn.ryoii.baka.Baka;

class Test {
    public static void main(String[] args) {
        Baka db = Baka.open("/db/baseDir");
        db.put("key", "value");
        String value = db.get("key");
        
        assert "value".equals(value);
        
        db.close();
    }
}
```

### Benchmark

```shell
git clone https://github.com/ryoii/baka.git
cd baka
./gradlew benchmark

> Benchmark                           Mode  Cnt      Score   Error  Units
> BenchWriteAndRead.launchBenchmark  thrpt    2  33709.215          ops/s
```

### TODO

+ [ ] More key type: int, long, iterable bytes, etc.
+ [ ] Support serializable value.
+ [ ] Expired key.
+ [ ] More data struct?
+ [ ] Art tree index manager for range search?