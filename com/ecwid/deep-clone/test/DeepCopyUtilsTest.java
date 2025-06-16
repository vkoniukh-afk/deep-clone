import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DeepCopyUtilsTest {

    @Test
    public void testPrimitiveTypes() {
        Integer originalInt = 42;
        Integer copiedInt = DeepCopyUtils.deepCopy(originalInt);
        assertEquals(originalInt, copiedInt);
        assertSame(originalInt, copiedInt);

        Double originalDouble = 3.14;
        Double copiedDouble = DeepCopyUtils.deepCopy(originalDouble);
        assertEquals(originalDouble, copiedDouble);
        assertSame(originalDouble, copiedDouble);
    }

    @Test
    public void testString() {
        String original = "Hello, World!";
        String copied = DeepCopyUtils.deepCopy(original);
        assertEquals(original, copied);
    }

    @Test
    public void testArrays() {
        int[] originalArray = {1, 2, 3, 4, 5};
        int[] copiedArray = DeepCopyUtils.deepCopy(originalArray);
        assertArrayEquals(originalArray, copiedArray);
        assertNotSame(originalArray, copiedArray);

        String[] originalStrArray = {"a", "b", "c"};
        String[] copiedStrArray = DeepCopyUtils.deepCopy(originalStrArray);
        assertArrayEquals(originalStrArray, copiedStrArray);
        assertNotSame(originalStrArray, copiedStrArray);
    }

    @Test
    public void testCollections() {
        List<Integer> originalList = Arrays.asList(1, 2, 3, 4);
        List<Integer> copiedList = DeepCopyUtils.deepCopy(originalList);
        assertEquals(originalList, copiedList);
        assertNotSame(originalList, copiedList);

        Set<String> originalSet = new HashSet<>(Arrays.asList("a", "b", "c"));
        Set<String> copiedSet = DeepCopyUtils.deepCopy(originalSet);
        assertEquals(originalSet, copiedSet);
        assertNotSame(originalSet, copiedSet);

        Map<String, Integer> originalMap = new HashMap<>();
        originalMap.put("one", 1);
        originalMap.put("two", 2);
        Map<String, Integer> copiedMap = DeepCopyUtils.deepCopy(originalMap);
        assertEquals(originalMap, copiedMap);
        assertNotSame(originalMap, copiedMap);
    }

    @Test
    public void testCustomObjects() {
        Man originalMan = new Man("John", 30);
        Man copiedMan = DeepCopyUtils.deepCopy(originalMan);

        assertEquals(originalMan.getName(), copiedMan.getName());
        assertEquals(originalMan.getAge(), copiedMan.getAge());
        assertNotSame(originalMan, copiedMan);
    }

    @Test
    public void testNestedStructures() {
        Map<String, List<Man>> original = new HashMap<>();
        original.put("team1", Arrays.asList(new Man("Alice", 25), new Man("Bob", 30)));
        original.put("team2", List.of(new Man("Charlie", 35)));

        Map<String, List<Man>> copied = DeepCopyUtils.deepCopy(original);

        assertEquals(original, copied);
        assertNotSame(original, copied);
        assertNotSame(original.get("team1"), copied.get("team1"));
        assertNotSame(original.get("team1").getFirst(), copied.get("team1").getFirst());
    }

    @Test
    public void testNull() {
        assertNull(DeepCopyUtils.deepCopy(null));
    }


    @Test
    public void testUtilDate() {
        Date original = new Date();
        Date copied = DeepCopyUtils.deepCopy(original);

        assertEquals(original, copied);
        assertNotSame(original, copied);
        assertEquals(original.getTime(), copied.getTime());
    }

    @Test
    public void testSqlDate() {
        java.sql.Date original = new java.sql.Date(System.currentTimeMillis());
        java.sql.Date copied = DeepCopyUtils.deepCopy(original);

        assertEquals(original, copied);
        assertNotSame(original, copied);
    }

    @Test
    public void testTimestamp() {
        Timestamp original = new Timestamp(System.currentTimeMillis());
        Timestamp copied = DeepCopyUtils.deepCopy(original);

        assertEquals(original, copied);
        assertNotSame(original, copied);
        assertEquals(original.getNanos(), copied.getNanos());
    }

    @Test
    public void testDateInObject() {
        class Event {
            Date date;
            Event(Date date) { this.date = date; }
        }

        Event original = new Event(new Date());
        Event copied = DeepCopyUtils.deepCopy(original);

        assertEquals(original.date, copied.date);
        assertNotSame(original.date, copied.date);
    }


    @Test
    public void testCopyOnWriteArrayList() {
        CopyOnWriteArrayList<Object> original = new CopyOnWriteArrayList<>(Arrays.asList(
                "string",
                42,
                new Date(),
                new CopyOnWriteArrayList<>(List.of("nested"))
        ));

        CopyOnWriteArrayList<Object> copied = DeepCopyUtils.deepCopy(original);

        assertNotSame(original, copied, "Copy should be new object");
        assertEquals(original.size(), copied.size(), "Sizes should be equals");

        for (int i = 0; i < original.size(); i++) {
            Object origElement = original.get(i);
            Object copiedElement = copied.get(i);

            assertEquals(origElement, copiedElement,
                    String.format("The elements %d should be equals", i));

            if (origElement != null && !DeepCopyUtils.isImmutable(origElement.getClass())) {
                assertNotSame(origElement, copiedElement,
                        String.format("The element %d should be a new object", i));
            }
        }

        assertTrue(copied.get(3) instanceof CopyOnWriteArrayList,
                "The nested list should be the same type");
        CopyOnWriteArrayList<?> nestedOriginal = (CopyOnWriteArrayList<?>) original.get(3);
        CopyOnWriteArrayList<?> nestedCopied = (CopyOnWriteArrayList<?>) copied.get(3);
        assertNotSame(nestedOriginal, nestedCopied,
                "The nested list should be the same type");
        assertEquals(nestedOriginal, nestedCopied,
                "The nested lists should be equals by nested objects");
    }

    @Test
    public void testEmptyCopyOnWriteArrayList() {
        CopyOnWriteArrayList<String> original = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<String> copied = DeepCopyUtils.deepCopy(original);

        assertNotSame(original, copied);
        assertTrue(copied.isEmpty());
    }

    @Test
    public void testCopyOnWriteArrayListWithNulls() {
        CopyOnWriteArrayList<Object> original = new CopyOnWriteArrayList<>(Arrays.asList(null, null));
        CopyOnWriteArrayList<Object> copied = DeepCopyUtils.deepCopy(original);

        assertNotSame(original, copied);
        assertEquals(2, copied.size());
        assertNull(copied.get(0));
        assertNull(copied.get(1));
    }


    @Test
    public void testComplexDateScenarios() {
        Date preEpoch = new Date(-1000L * 60 * 60 * 24 * 365); // ~1969 год
        testDateCopy(preEpoch, "The date before 1970");

        Date withMillis = new Date(1672531200123L); // 2023-01-01 00:00:00.123
        testDateCopy(withMillis, "Ms date");

        Date farFuture = new Date(Long.MAX_VALUE - 1);
        testDateCopy(farFuture, "Max date");

        Date farPast = new Date(Long.MIN_VALUE);
        testDateCopy(farPast, "Min date");

        Timestamp nowWithNanos = new Timestamp(System.currentTimeMillis());
        nowWithNanos.setNanos(123456789);
        testDateCopy(nowWithNanos, "Timestamp ms");

        TimeZone defaultTZ = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("GMT+12"));
            Date dateInGMT12 = new Date();
            testDateCopy(dateInGMT12, "GMT+12");

            TimeZone.setDefault(TimeZone.getTimeZone("GMT-8"));
            Date dateInGMTMinus8 = new Date();
            testDateCopy(dateInGMTMinus8, "GMT-8");
        } finally {
            TimeZone.setDefault(defaultTZ);
        }
    }

    private void testDateCopy(Date original, String description) {
        Date copied = DeepCopyUtils.deepCopy(original);

        assertNotSame(original, copied, description + ": the time should be a new object");
        assertEquals(original.getTime(), copied.getTime(),
                description + ": the time should be equal");

        if (original instanceof Timestamp) {
            assertEquals(((Timestamp)original).getNanos(),
                    ((Timestamp)copied).getNanos(),
                    description + ": the time in nano sec should be equal");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");
        assertEquals(sdf.format(original), sdf.format(copied),
                description + ": the format should be equals");
    }


    @Test
    public void testDateInDifferentTimeZones() {
        Date original = new Date();

        for (String tzId : Arrays.asList("GMT", "GMT+12", "GMT-8", "Asia/Tokyo", "Europe/Moscow")) {
            TimeZone.setDefault(TimeZone.getTimeZone(tzId));

            Date copied = DeepCopyUtils.deepCopy(original);

            assertEquals(original.getTime(), copied.getTime(),
                    "Time zone '" + tzId + "' should not affect copying");

            assertNotSame(original, copied,
                    "Copy in timezone '" + tzId + "' should be a new object");
        }
    }

    @Test
    public void testDateInCompositeObject() {
        class Event {
            Date startDate;
            Date endDate;
            String name;

            Event(Date start, Date end, String name) {
                this.startDate = start;
                this.endDate = end;
                this.name = name;
            }
        }

        Date start = new Date();
        Date end = new Date(start.getTime() + TimeUnit.DAYS.toMillis(1));
        Event original = new Event(start, end, "Conference");

        Event copied = DeepCopyUtils.deepCopy(original);

        assertNotSame(original, copied);
        assertEquals(original.name, copied.name);

        assertNotSame(original.startDate, copied.startDate);
        assertEquals(original.startDate.getTime(), copied.startDate.getTime());

        assertNotSame(original.endDate, copied.endDate);
        assertEquals(original.endDate.getTime(), copied.endDate.getTime());
    }


    public static class Man {
        private String name;
        private int age;
        private List<String> favoriteBooks;

        public Man(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Man man = (Man) o;
            return age == man.age && Objects.equals(name, man.name) && Objects.equals(favoriteBooks, man.favoriteBooks);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age, favoriteBooks);
        }

        public Man(String name, int age, List<String> favoriteBooks) {
            this.name = name;
            this.age = age;
            this.favoriteBooks = favoriteBooks;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public List<String> getFavoriteBooks() {
            return favoriteBooks;
        }

        public void setFavoriteBooks(List<String> favoriteBooks) {
            this.favoriteBooks = favoriteBooks;
        }

        @Override
        public String toString() {
            return "Man{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    ", favoriteBooks=" + favoriteBooks +
                    '}';
        }
    }
}