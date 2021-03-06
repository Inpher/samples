import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by alex on 12/16/16.
 */
public class SmartDoor {
    private static String csvFilePeople;
    private static String csvFileDoors;
    private static List<Person> people;
    private static List<Door> doors;

    private static List<Door> getDoorsFromFile() {
        try (Stream<String> stream = Files.lines(Paths.get(csvFileDoors))) {
            return stream.map(s -> new Door(s.split(","))).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static List<Person> getPeopleFromFile() {
        try (Stream<String> stream = Files.lines(Paths.get(csvFilePeople))) {
            return stream.map(s -> new Person(s.split(","))).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args){
        if (args.length > 0) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(new File(args[0]));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            try {
                csvFileDoors = br.readLine();
                csvFilePeople = br.readLine();
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            people = getPeopleFromFile();
            doors = getDoorsFromFile();
        }
        else{
            csvFilePeople = SmartDoor.class.getResource("/person.csv").getPath();
            csvFileDoors = SmartDoor.class.getResource("/door.csv").getPath();
        }
        people = getPeopleFromFile();
        doors = getDoorsFromFile();
        Timer timer = new Timer();
        timer.schedule(new Access(people,doors), 0,  (int) (10000 + Math.random()*10000));
    }
}

class Access extends TimerTask {

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private final List<Door> doors;
    private final List<Person> people;
    private Map jsonClear;

    public Access(List<Person> people, List<Door> doors) {this.people = people;
    this.doors = doors;
    }


    public void run() {
        // Generate json
        generateJson();
        System.out.println("SMART DOOR: DEBUG cleartext event: \n" + jsonClear +"\n");

        // Encrypt and send json to Encryption Proxy
        try {
            EncryptionProxy.encryptAndIndexJSON(jsonClear);
        } catch (ParseException e) {
            e.printStackTrace();
            System.out.println("Could not parse date string!");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not parse Map!");
        }
    }

    private void generateJson() {
        Map person = people.get((int) (Math.random()*people.size())).getPerson();
        Map door = doors.get((int) (Math.random()*doors.size())).getDoor();

        jsonClear = new HashMap();
        jsonClear.putAll(person);
        jsonClear.putAll(door);
        jsonClear.putAll(getTime());
    }


    private static Map getTime(){
        Date date = new Date();
        Map time = new HashMap();
        time.put("time", dateFormat.format(date));
        return Collections.unmodifiableMap(time);
    }
}

class Door{
    private Map door;

    public Door(String[] door){
        if (door.length != 3) throw new RuntimeException("Could not read line (door)!");
        this.door = new HashMap();
        this.door.put("door_id", door[0]);
        this.door.put("building", door[1]);
        this.door.put("zip", door[2]);
    }

    public Map getDoor(){
        return Collections.unmodifiableMap(door);
    }
}

class Person {
    private Map person;

    public Person(String[] person){
        if (person.length != 3) throw new RuntimeException("Could not read line (door)!");
        this.person = new HashMap();
        this.person.put("id", person[0]);
        this.person.put("first_name", person[1]);
        this.person.put("last_name", person[2]);
    }

    public Map getPerson(){
        return Collections.unmodifiableMap(person);
    }
}

