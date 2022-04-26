import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class FlightScheduler {
    private static FlightScheduler instance;
    public Map<Integer, Flight> flights = new TreeMap<Integer, Flight>();
    public Map<String, Location> locations = new TreeMap<String, Location>();
    public static final String[] sWeek = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};

    private int flightID;
    DecimalFormat decimalFormat = new DecimalFormat("##.00");

    public FlightScheduler() {

    }

    public static void main(String[] args) {
        instance = new FlightScheduler(args);

        instance.run();
    }

    public static FlightScheduler getInstance() {
        return instance;
    }

    public FlightScheduler(String[] args) {


    }

    public void run() {
        // Do not use System.exit() anywhere in your code,
        // otherwise it will also exit the auto test suite.
        // Also, do not use static attributes otherwise
        // they will maintain the same values between testcases.

        // START YOUR CODE HERE
        String s;
        Scanner scanner = new Scanner(System.in);
        do {
            System.out.print("User: ");
            s = scanner.nextLine();
            decoder(s);
        } while (!s.toLowerCase().equals("exit"));
        System.out.println("Application closed.");


    }

    public void decoder(String s) {
        String[] str = s.split(" ");
        switch (str[0].toLowerCase()) {
            case "flights", "flight" -> {
                commandFlight(str);
            }

            case "locations", "location" -> {
                commandLocation(str);
            }

            case "travel" -> {
                commandTravel(str);
            }

            case "exit" -> {

            }

            case "test" -> {
                test();
            }

            default -> {
                commandDefault(str);
            }
        }

    }

    private void test() {
        importLocations("location import locations.csv".split(" "));
        importLocations("location import locations4.csv".split(" "));
        importFlights("flight import flights6.csv".split(" "));
        ArrayList<ArrayList<Flight>> list = travelPathPlus(locations.get("sydney"), locations.get("london"));
        Collections.sort(list, sortByCost);
        for (ArrayList<Flight> list1 : list) {
            for (Flight flight : list1)
                System.out.print(flight.getFlightID() + " ");
            System.out.println(getTotal(list1, "c") + " " + fomatLayTime(getTotal(list1, "d")));
        }

    }

    private void commandFlight(String[] s) {

        if (s[0].equalsIgnoreCase("flights")) {
            showFlights();
        } else if (s[0].equalsIgnoreCase("flight") && s.length == 1) {
            System.out.println("Usage:" +
                    "\nFLIGHT <id> [BOOK/REMOVE/RESET] [num]" +
                    "\nFLIGHT ADD <departure time> <from> <to> <capacity>" +
                    "\nFLIGHT IMPORT/EXPORT <filename>\n");
        } else {

            switch (s[1].toLowerCase()) {
                case "import" -> {
                    importFlights(s);
                }
                case "export" -> {
                    exportFlights(s);
                }

                case "add" -> {
                    //add
                    addFlightOut(s);
                }
                default -> {
                    //flight id
                    int id = -1;
                    try {
                        id = Integer.parseInt(s[1]);
                        if (id < 0) {
                            System.out.println("Invalid Flight ID.\n");
                            break;
                        }
                        if (!flights.containsKey(id)) {
                            System.out.println("Invalid Flight ID.\n");
                            break;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid Flight ID.\n");
                        break;
                    }

                    if (s.length == 2) {
                        if (s[0].equalsIgnoreCase("flight"))
                            flightId(s);
                    }

                    if (s.length >= 3) {
                        //reset
                        if (s[2].equalsIgnoreCase("reset")) {
                            flightReset(s);
                        }
                        //remove
                        else if (s[2].equalsIgnoreCase("remove")) {
                            flightRemove(s);
                        } else if (s.length >= 4 && !s[2].equalsIgnoreCase("book")) {
                            flightId(s);
                        } else if (s[2].equalsIgnoreCase("book") && s.length == 3)
                            flightBook(s);
                    }

                    if (s.length >= 4) {
                        //book
                        if (s[2].equalsIgnoreCase("book")) {
                            flightBook(s);
                        }
                    }
                }
            }
        }
    }

// Add a flight to the database
// handle error cases and return status negative if error
// (different status codes for different messages)
// do not print out anything in this function


    public void addFlightOut(String[] s) {
        if (s.length < 7) {
            System.out.println("Usage:   FLIGHT ADD <departure time> <from> <to> <capacity>\n" +
                    "Example: FLIGHT ADD Monday 18:00 Sydney Melbourne 120\n");
        } else {
            int nums = addFlight(s[2], s[3], s[4], s[5], s[6], 0);
            switch (nums) {
                case -1 -> {
                    System.out.println("Invalid departure time. " +
                            "Use the format <day_of_week> <hour:minute>, with 24h time.\n");
                }
                case -2 -> {
                    System.out.println("Invalid starting location.\n");
                }
                case -3 -> {
                    System.out.println("Invalid ending location.\n");
                }
                case -4 -> {
                    System.out.println("Invalid positive integer capacity.\n");
                }
                case -5 -> {
                    System.out.println("Source and destination cannot be the same place.\n");
                }
                case -6 -> {
                    int[] dateInt = new int[2];
                    String[] stest = s[3].split(":");
                    dateInt[0] = Integer.parseInt(stest[0]);
                    dateInt[1] = Integer.parseInt(stest[1]);
                    Flight f = new Flight(s[2], dateInt, setLocation(s[4]), setLocation(s[5]), Integer.parseInt(s[6]), 0, -1);
                    System.out.println("Scheduling conflict! This flight clashes with " + locations.get(s[4].toLowerCase()).hasRunwayDepartureSpace(f) + ".\n");
                }
                case -7 -> {
                    int[] dateInt = new int[2];
                    String[] stest = s[3].split(":");
                    dateInt[0] = Integer.parseInt(stest[0]);
                    dateInt[1] = Integer.parseInt(stest[1]);
                    Flight f = new Flight(s[2], dateInt, setLocation(s[4]), setLocation(s[5]), Integer.parseInt(s[6]), 0, -1);
                    System.out.println("Scheduling conflict! This flight clashes with " + locations.get(s[5].toLowerCase()).hasRunwayArrivalSpace(f) + ".\n");
                }
                case 1 -> {
                    System.out.println("Successfully added Flight " + (flightID - 1) + ".\n");
                }
            }
        }

    }


    public int addFlight(String date1, String date2, String start, String end, String capacity, int booked) {
        int[] dateInt = new int[2];
        int capacityInt;

        //time check
        try {
            if (!date2.contains(":")) {
                return -1;
            }

            String[] stest = date2.replace(" ", "").split(":");
            dateInt[0] = Integer.parseInt(stest[0]);
            dateInt[1] = Integer.parseInt(stest[1]);

            if (!containWeek(date1)) {
                return -1;
            }

            if (!(dateInt[0] >= 0 && dateInt[0] < 24 && dateInt[1] >= 0 && dateInt[1] < 60)) {
                return -1;
            }

        } catch (NumberFormatException e) {
            return -1;
        }

        //start end check
        if (!locations.containsKey(start.toLowerCase())) {
            return -2;
        }

        if (!locations.containsKey(end.toLowerCase())) {
            return -3;
        }

        //capacity check
        try {
            capacityInt = Integer.parseInt(capacity);
            if (capacityInt <= 0) {
                return -4;
            }
        } catch (NumberFormatException e) {
            return -4;
        }

        if (start.equalsIgnoreCase(end)) {
            return -5;
        }


        Flight f = new Flight(date1.toLowerCase(), dateInt, setLocation(start), setLocation(end), capacityInt, booked, flightID);
        if (locations.get(start.toLowerCase()).hasRunwayDepartureSpace(f) != null) {
            return -6;
        }

        if (locations.get(end.toLowerCase()).hasRunwayArrivalSpace(f) != null) {
            return -7;
        }

        flights.put(flightID, f);
        flightID++;
        locations.get(start.toLowerCase()).addDeparture(f);
        locations.get(end.toLowerCase()).addArrival(f);
        return 1;
    }

    private void showFlights() {
        System.out.println("Flights");
        System.out.println("-------------------------------------------------------");
        System.out.println("ID   Departure   Arrival     Source --> Destination");
        System.out.println("-------------------------------------------------------");

        if (flights.size() > 0) {
            ArrayList<Flight> allflight = new ArrayList<Flight>();
            allflight.addAll(flights.values());
            Collections.sort(allflight, sortFlight);
            for (Flight i : allflight) {
                System.out.printf("%4d%10s%12s   %s\n"
                        , i.getFlightID(), i.getDep(), i.getArr(), (i.getStart() + " --> " + i.getEnd()));
            }
        } else {
            System.out.println("(None)");
        }
        System.out.println();
    }

    Comparator<Flight> sortFlight = new Comparator<Flight>() {
        @Override
        public int compare(Flight o1, Flight o2) {
            if (o1.getDepInt() != o2.getDepInt()) {
                return Double.compare(o1.getDepInt(), o2.getDepInt());
            } else {
                return sortSpring.compare(o1.getStart(), o2.getStart());
            }
        }
    };
    Comparator<String> sortSpring = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            int s1 = (int) o1.charAt(0);
            int s2 = (int) o2.charAt(0);

            return Double.compare(s1, s2);
        }
    };

    public void findFlight(int nums) {
        if (flights.containsKey(nums)) {
            Flight i = flights.get(nums);
            System.out.println("Flight " + nums);
            System.out.printf("%-14s%-10s%s\n"
                    , "Departure:", i.getDep(), i.getStart());
            System.out.printf("%-14s%-10s%s\n"
                    , "Arrival:", i.getArr(), i.getEnd());
            System.out.printf("%-14s%s\n"
                    , "Distance:", String.format("%,d", Math.round(i.getDistance())) + "km");
            System.out.printf("%-14s%-3s%d%s\n"
                    , "Duration:", Integer.toString(i.getDuration() / 60) + "h ", i.getDuration() % 60, "m");
            System.out.printf("%-14s%s\n"
                    , "Ticket Cost:", "$" + decimalFormat.format(i.getTicketPrice()));
            System.out.printf("%-14s%s\n"
                    , "Passengers:", i.getCapacity());
            System.out.println();
        }
    }

    public void flightId(String[] s) {
        try {
            int id;
            id = Integer.parseInt(s[1]);
            if (id < 0) {
                System.out.println("Invalid Flight ID.\n");
                return;
            }

            if (flights.containsKey(id)) {
                findFlight(id);
            } else {
                System.out.println("Invalid Flight ID.\n");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid Flight ID.\n");
        }
    }

    public void flightReset(String[] s) {
        int id = Integer.parseInt(s[1]);
        Iterator<Map.Entry<Integer, Flight>> iterator = flights.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Flight> entry = iterator.next();
            if (entry.getKey() == id) {
                Flight flight = entry.getValue();
                System.out.println("Reset passengers booked to 0 for Flight " + id + ", " + flight.getDep() +
                        " " + flight.getStart() + " --> " + flight.getEnd() + ".\n");
                flight.reset();
            }
        }
    }

    public void flightRemove(String[] s) {
        try {
            int id = Integer.parseInt(s[1]);
            if (flights.containsKey(id)) {
                Flight f = flights.get(id);
                int rfid = f.getFlightID();
                String dayTime = f.getDep();
                String st = f.getStart();
                String ed = f.getEnd();
                locations.get(f.getStart().toLowerCase()).remDeparture(f);
                locations.get(f.getEnd().toLowerCase()).remArrival(f);
                flights.remove(id);
                System.out.println("Removed Flight " + rfid + ", " + dayTime + " " + st +
                        " --> " + ed + ", from the flight schedule.\n");
            }
        } catch (NumberFormatException e) {
            return;
        }
    }

    public void flightBook(String[] s) {
        int id = Integer.parseInt(s[1]);
        int nums = 1;
        try {
            nums = Integer.parseInt(s[3]);
            if (nums <= 0) {
                System.out.println("Invalid number of passengers to book.\n");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number of passengers to book.\n");
            return;
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }

        if (flights.containsKey(id)) {
            Flight f = flights.get(id);
            if (f.isFull()) {
                System.out.println("Booked 0 passengers on flight "
                        + f.getFlightID() + " for a total cost of $0.00");
                System.out.println("Flight is now full.\n");
            } else if (f.getNumberOfBook() + nums - f.getFull() > 0) {
                int count = f.getFull() - f.getNumberOfBook();
                System.out.println("Booked " + count + " passengers on flight "
                        + f.getFlightID() + " for a total cost of $" + decimalFormat.format(f.book(count)));
                System.out.println("Flight is now full.\n");
            } else {
                System.out.println("Booked " + nums + " passengers on flight "
                        + f.getFlightID() + " for a total cost of $" + decimalFormat.format(f.book(nums)));
                if (f.isFull())
                    System.out.println("Flight is now full.\n");
                else
                    System.out.println();
            }
        }

    }

    public boolean containWeek(String s) {
        String s2 = s.replace(" ", "");
        for (String i : sWeek) {
            if (i.equalsIgnoreCase(s2)) {
                return true;
            }
        }
        return false;
    }

    public Location setLocation(String s) {
        if (locations.containsKey(s.toLowerCase())) {
            Location location = locations.get(s.toLowerCase());
            return new Location(location.getName(), location.getLatitude(), location.getLongitude(), location.getDemand());
        }
        return null;
    }


    private void commandLocation(String[] s) {

        if (s.length < 2) {
            if (s[0].equalsIgnoreCase("locations")) {
                showLocations();
            }
            if (s[0].equalsIgnoreCase("location") && s.length == 1) {
                System.out.println("Usage:" +
                        "\nLOCATION <name>" +
                        "\nLOCATION ADD <name> <latitude> <longitude> <demand_coefficient>" +
                        "\nLOCATION IMPORT/EXPORT <filename>\n");
            }

        } else {
            switch (s[1].toLowerCase()) {
                case "add" -> {
                    if (s.length < 6) {
                        System.out.println("Usage:   LOCATION ADD <name> <lat> <long> <demand_coefficient>" +
                                "\nExample: LOCATION ADD Sydney -33.847927 150.651786 0.2\n");
                    } else {
                        int nums = addLocation(s[2], s[3], s[4], s[5]);
                        switch (nums) {
                            case -1 -> {
                                System.out.println("This location already exists.\n");
                            }
                            case -2 -> {
                                System.out.println("Invalid latitude. It must be a number of degrees between -85 and +85.\n");
                            }
                            case -3 -> {
                                System.out.println("Invalid longitude. It must be a number of degrees between -180 and +180.\n");
                            }
                            case -4 -> {
                                System.out.println("Invalid demand coefficient. It must be a number between -1 and +1.\n");
                            }
                            case 1 -> {
                                System.out.println("Successfully added location " + s[2] + ".\n");
                            }

                        }
                    }
                }
                case "import" -> {
                    importLocations(s);
                }
                case "export" -> {
                    exportLocation(s);
                }
                default -> {
                    //LOCATION <name>
                    locationName(s[1]);
                }
            }
        }
    }

    // Add a location to the database
// do not print out anything in this function
// return negative numbers for error cases
    public int addLocation(String name, String lat, String lon, String demand) {
        double latitude;
        double longitude;
        double dem;

        if (locations.containsKey(name.toLowerCase())) {
            return -1;
        }

        try {
            latitude = Double.parseDouble(lat);
            if (latitude < -85 || latitude > 85) {
                return -2;
            }
        } catch (NumberFormatException e) {
            return -2;
        }

        try {
            longitude = Double.parseDouble(lon);
            if (longitude < -180 || longitude > 180) {
                return -3;
            }
        } catch (NumberFormatException e) {
            return -3;
        }

        try {
            dem = Double.parseDouble(demand);
            if (dem < -1 || dem > 1) {
                return -4;
            }

        } catch (NumberFormatException e) {
            return -4;
        }

        locations.put(name.toLowerCase(), (new Location(name, latitude, longitude, dem)));
        return 1;

    }

    public void showLocations() {
        int count = 0;
        System.out.println("Locations (" + locations.size() + "):");

        if (locations.size() > 0) {
            for (Map.Entry<String, Location> i : locations.entrySet()) {
                System.out.print(i.getValue().getName());
                if (count < locations.size() - 1) {
                    System.out.print(", ");
                    count++;
                }
            }
            System.out.println();
        } else {
            System.out.println("(None)");
        }
        System.out.println();
    }

    public void locationName(String s) {
        String findLocation = s.toLowerCase();
        if (locations.containsKey(findLocation)) {
            Location location = locations.get(findLocation);
            System.out.printf("%-13s%s\n", "Location:", location.getName());
            System.out.printf("%-13s%f\n", "Latitude:", location.getLatitude());
            System.out.printf("%-13s%f\n", "Longitude:", location.getLongitude());
            System.out.printf("%-13s%+.4f\n", "Demand:", location.getDemand());
            System.out.println();
        } else {
            System.out.println("Invalid location name.\n");
        }
    }

    private void commandTravel(String[] s) {
        Location from = null;
        Location to = null;
        ArrayList<ArrayList<Flight>> list = new ArrayList<ArrayList<Flight>>();
        int n = 0;
        if (s.length == 1) {
            System.out.println("Usage: TRAVEL <from> <to> [cost/duration/stopovers/layover/flight_time]\n");
        }

        if (s.length >= 3) {
            if (!locations.containsKey(s[1].toLowerCase())) {
                System.out.println("Starting location not found.\n");
                return;
            }
            if (!locations.containsKey(s[2].toLowerCase())) {
                System.out.println("Ending location not found.\n");
                return;
            }
            from = locations.get(s[1].toLowerCase());
            to = locations.get(s[2].toLowerCase());

            //default -1
            list = travelPathPlus(from, to);

            if (list == null) {
                System.out.println("Sorry, no flights with 3 or less stopovers are available from " + from.getName() + " to " + to.getName() + ".\n");
                return;
            }

        }
        if (s.length >= 4) {

            if (s.length >= 5) {
                try {
                    n = Integer.parseInt(s[4]);
                    if (n<0)
                        n=0;

                } catch (NumberFormatException e) {
                } catch (ArrayIndexOutOfBoundsException ignored) {
                }
            }

            list = travelPathPlus(from, to);

            switch (s[3]) {
                case "cost" -> {
                    travelCost(list, n);
                }
                case "duration" -> {
                    travelDuration(list, n);
                }
                case "stopovers" -> {
                    travelStopovers(list, n);
                }
                case "layover" -> {
                    travelLayover(list, n);
                }
                case "flight_time" -> {
                    travelFlightTime(list, n);
                }
                default -> {
                    System.out.println("Invalid sorting property: must be either cost, duration, stopovers, layover, or flight_time.\n");
                }
            }
        } else {
            if (s.length == 3)
                travelDuration(list, n);
            else
                System.out.println("Usage: TRAVEL <from> <to> [cost/duration/stopovers/layover/flight_time]\n");
        }
    }

    public void travelFlightTime(ArrayList<ArrayList<Flight>> list, int n) {

        Collections.sort(list, sortByFlightTime);
        ArrayList<Flight> ans = new ArrayList<Flight>();
        if (list.size()>n)
            ans = list.get(n);
        else
            ans = list.get(list.size() - 1);

        System.out.printf("%-18s%d\n", "Legs:", ans.size());
        System.out.printf("%-18s%s\n", "Total Duration:", fomatLayTime(getTotal(ans, "d")));
        System.out.printf("%-18s$%s\n", "Total Cost:", decimalFormat.format(getTotal(ans, "c")));
        printTravel(ans);

    }

    public void travelLayover(ArrayList<ArrayList<Flight>> list, int n) {

        Collections.sort(list, sortByLayover);
        ArrayList<Flight> ans = new ArrayList<Flight>();
        if (list.size()>n)
            ans = list.get(n);
        else
            ans = list.get(list.size() - 1);

        System.out.printf("%-18s%d\n", "Legs:", ans.size());
        System.out.printf("%-18s%s\n", "Total Duration:", fomatLayTime(getTotal(ans, "d")));
        System.out.printf("%-18s$%s\n", "Total Cost:", decimalFormat.format(getTotal(ans, "c")));
        printTravel(ans);
    }



    public void travelStopovers(ArrayList<ArrayList<Flight>> list, int n) {
        Collections.sort(list, sortByStopovers);
        ArrayList<Flight> ans = new ArrayList<Flight>();
        if (list.size()>n)
            ans = list.get(n);
        else
            ans = list.get(list.size() - 1);

        System.out.printf("%-18s%d\n", "Legs:", ans.size());
        System.out.printf("%-18s%s\n", "Total Duration:", fomatLayTime(getTotal(ans, "d")));
        System.out.printf("%-18s$%s\n", "Total Cost:", decimalFormat.format(getTotal(ans, "c")));
        printTravel(ans);
    }

    public void travelCost(ArrayList<ArrayList<Flight>> list, int n) {
        Collections.sort(list, sortByCost);
        ArrayList<Flight> ans = new ArrayList<Flight>();
        if (list.size()>n)
            ans = list.get(n);
        else
            ans = list.get(list.size() - 1);

        System.out.printf("%-18s%d\n", "Legs:", ans.size());
        System.out.printf("%-18s%s\n", "Total Duration:", fomatLayTime(getTotal(ans, "d")));
        System.out.printf("%-18s$%s\n", "Total Cost:", decimalFormat.format(getTotal(ans, "c")));
        printTravel(ans);
    }

    public void travelDuration(ArrayList<ArrayList<Flight>> list, int n) {
        Collections.sort(list, sortByDuration);
        ArrayList<Flight> ans = new ArrayList<Flight>();
        if (list.size()>n)
            ans = list.get(n);
        else
            ans = list.get(list.size() - 1);

        System.out.printf("%-18s%d\n", "Legs:", ans.size());
        System.out.printf("%-18s%s\n", "Total Duration:", fomatLayTime(getTotal(ans, "d")));
        System.out.printf("%-18s$%s\n", "Total Cost:", decimalFormat.format(getTotal(ans, "c")));
        printTravel(ans);
    }

    public void printTravel(ArrayList<Flight> list) {

        System.out.println("-------------------------------------------------------------");
        System.out.printf("%s%7s%15s%10s%11s --> %s\n", "ID", "Cost", "Departure", "Arrival", "Source", "Destination");
        System.out.println("-------------------------------------------------------------");
        for (int i = 0; i < list.size(); i++) {
            Flight f = list.get(i);
            if (i != list.size() - 1) {
                System.out.printf("%4d $%8s%10s%12s   %s --> %s\n", f.getFlightID(), decimalFormat.format(f.getTicketPrice()), f.getDep()
                        , f.getArr(), f.getStart(), f.getEnd());
                System.out.printf("LAYOVER %s at %s\n", fomatLayTime(Flight.layover(list.get(i), list.get(i + 1))), f.getEnd());
            } else {
                System.out.printf("%4d $%8s%10s%12s   %s --> %s\n\n", f.getFlightID(), decimalFormat.format(f.getTicketPrice()), f.getDep()
                        , f.getArr(), f.getStart(), f.getEnd());
            }
        }
    }

    public String fomatLayTime(double time) {
        int timeint = (int) time;
        int hour = 0;
        int minute = 0;
        hour = timeint / 60;
        minute = timeint % 60;
        return new String(String.format("%dh %dm", hour, minute));
    }

    public double getTotal(ArrayList<Flight> list, String s) {
        if (s.equalsIgnoreCase("c")) {
            double cost = 0;
            for (int i = 0; i < list.size(); i++) {
                cost += list.get(i).getTicketPrice();
            }
            return cost;
        }
        if (s.equalsIgnoreCase("d")) {
            int duration = 0;
            for (int i = 0; i < list.size(); i++) {
                if (i != list.size() - 1)
                    duration += list.get(i).getDuration() + Flight.layover(list.get(i), list.get(i + 1));
                else
                    duration += list.get(i).getDuration();
            }
            return duration;
        }
        if (s.equalsIgnoreCase("s")) {
            int stopovers = 0;
            stopovers = list.size() - 1;
            return stopovers;
        }
        if (s.equalsIgnoreCase("l")) {
            int layover = 0;
            for (int i = 0; i < list.size(); i++) {
                if (i != list.size() - 1)
                    layover += Flight.layover(list.get(i), list.get(i + 1));
            }
            return layover;
        }
        if (s.equalsIgnoreCase("f")) {
            int flight_time = 0;
            for (int i = 0; i < list.size(); i++) {
                flight_time += list.get(i).getDuration();
            }
            return flight_time;
        }
        return -1;
    }

    Comparator<ArrayList<Flight>> sortByCost = new Comparator<ArrayList<Flight>>() {
        @Override
        public int compare(ArrayList<Flight> o1, ArrayList<Flight> o2) {

            if (getTotal(o1, "c") != getTotal(o2, "c"))
                return Double.compare(getTotal(o1, "c"), getTotal(o2, "c"));
            else
                return Double.compare(getTotal(o1, "d"), getTotal(o2, "d"));
        }
    };

    Comparator<ArrayList<Flight>> sortByDuration = new Comparator<ArrayList<Flight>>() {
        @Override
        public int compare(ArrayList<Flight> o1, ArrayList<Flight> o2) {

            if (getTotal(o1, "d") != getTotal(o2, "d"))
                return Double.compare(getTotal(o1, "d"), getTotal(o2, "d"));
            else
                return Double.compare(getTotal(o1, "c"), getTotal(o2, "c"));
        }
    };

    Comparator<ArrayList<Flight>> sortByStopovers = new Comparator<ArrayList<Flight>>() {
        @Override
        public int compare(ArrayList<Flight> o1, ArrayList<Flight> o2) {
            if (getTotal(o1, "s") != getTotal(o2, "s"))
                return Double.compare(getTotal(o1, "s"), getTotal(o2, "s"));
            else
                return sortByDuration.compare(o1, o2);
        }
    };

    Comparator<ArrayList<Flight>> sortByLayover = new Comparator<ArrayList<Flight>>() {
        @Override
        public int compare(ArrayList<Flight> o1, ArrayList<Flight> o2) {
            double o1Layover = 0;
            double o2Layover = 0;
            for (int i = 0; i < o1.size(); i++) {
                if (i != o1.size() - 1)
                    o1Layover += Flight.layover(o1.get(i), o1.get(i + 1));
            }

            for (int i = 0; i < o2.size(); i++) {
                if (i != o2.size() - 1)
                    o2Layover += Flight.layover(o2.get(i), o2.get(i + 1));
            }
            if (o1Layover != o2Layover)
                return Double.compare(o1Layover, o2Layover);
            else
                return sortByDuration.compare(o1, o2);
        }
    };

    Comparator<ArrayList<Flight>> sortByFlightTime = new Comparator<ArrayList<Flight>>() {
        @Override
        public int compare(ArrayList<Flight> o1, ArrayList<Flight> o2) {
            double o1FlightTime = 0;
            double o2FlightTime = 0;
            o1FlightTime = getTotal(o1, "f");
            o2FlightTime = getTotal(o2, "f");
            if (o1FlightTime != o2FlightTime)
                return Double.compare(o1FlightTime, o2FlightTime);
            else
                return sortByDuration.compare(o1, o2);
        }
    };

    public ArrayList<ArrayList<Flight>> travelPathPlus(Location from, Location to) {
        ArrayList<ArrayList<Flight>> anslist = new ArrayList<ArrayList<Flight>>();
        Location start = from;
        for (Flight flight1 : start.dep) {
            if (flight1.getEnd().equalsIgnoreCase(to.getName())) {
                ArrayList<Flight> fromlist1 = new ArrayList<Flight>();
                fromlist1.add(flight1);
                anslist.add(fromlist1);
            } else {
                start = locations.get(flight1.getEnd().toLowerCase());
                for (Flight flight2 : start.dep) {
                    if (flight2.getEnd().equalsIgnoreCase(to.getName())) {
                        ArrayList<Flight> fromlist2 = new ArrayList<Flight>();
                        fromlist2.add(flight1);
                        fromlist2.add(flight2);
                        anslist.add(fromlist2);
                    } else {
                        start = locations.get(flight2.getEnd().toLowerCase());
                        for (Flight flight3 : start.dep) {
                            if (flight3.getEnd().equalsIgnoreCase(to.getName())) {
                                ArrayList<Flight> fromlist3 = new ArrayList<Flight>();
                                fromlist3.add(flight1);
                                fromlist3.add(flight2);
                                fromlist3.add(flight3);
                                anslist.add(fromlist3);
                            } else {
                                start = locations.get(flight3.getEnd().toLowerCase());
                                for (Flight flight4 : start.dep) {
                                    if (flight4.getEnd().equalsIgnoreCase(to.getName())) {
                                        ArrayList<Flight> fromlist4 = new ArrayList<Flight>();
                                        fromlist4.add(flight1);
                                        fromlist4.add(flight2);
                                        fromlist4.add(flight3);
                                        fromlist4.add(flight4);
                                        anslist.add(fromlist4);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (anslist.size() != 0)
            return anslist;
        else
            return null;
    }

    private void commandDefault(String[] s) {
        switch (s[0].toLowerCase()) {
            case "schedule" -> {
                if (s.length >= 2) {
                    if (locations.containsKey(s[1].toLowerCase()))
                        locations.get(s[1].toLowerCase()).schedule("s");
                    else
                        System.out.println("This location does not exist in the system.\n");

                } else {
                    System.out.println("This location does not exist in the system.\n");
                }
            }
            case "departures" -> {
                if (s.length >= 2) {
                    if (locations.containsKey(s[1].toLowerCase()))
                        locations.get(s[1].toLowerCase()).schedule("d");
                    else
                        System.out.println("This location does not exist in the system.\n");

                } else {
                    System.out.println("This location does not exist in the system.\n");
                }

            }
            case "arrivals" -> {
                if (s.length >= 2) {
                    if (locations.containsKey(s[1].toLowerCase()))
                        locations.get(s[1].toLowerCase()).schedule("a");
                    else
                        System.out.println("This location does not exist in the system.\n");

                } else {
                    System.out.println("This location does not exist in the system.\n");
                }

            }
            case "help" -> {
                help();
            }

            default -> {
                System.out.println("Invalid command. Type 'help' for a list of commands.\n");
            }
        }

    }


    private void help() {
        System.out.println("FLIGHTS - list all available flights ordered by departure time, then departure location name\n" +
                "FLIGHT ADD <departure time> <from> <to> <capacity> - add a flight\n" +
                "FLIGHT IMPORT/EXPORT <filename> - import/export flights to csv file\n" +
                "FLIGHT <id> - view information about a flight (from->to, departure arrival times, current ticket price, " +
                "capacity, passengers booked)\n" +
                "FLIGHT <id> BOOK <num> - book a certain number of passengers for the flight at the current ticket price, " +
                "and then adjust the ticket price to reflect the reduced capacity remaining. If no number is given, book 1 " +
                "passenger. If the given number of bookings is more than the remaining capacity, only accept bookings " +
                "until the capacity is full.\n" +
                "FLIGHT <id> REMOVE - remove a flight from the schedule\n" +
                "FLIGHT <id> RESET - reset the number of passengers booked to 0, and the ticket price to its original state.\n\n" +
                "LOCATIONS - list all available locations in alphabetical order\n" +
                "LOCATION ADD <name> <lat> <long> <demand_coefficient> - add a location\n" +
                "LOCATION <name> - view details about a location (it's name, coordinates, demand coefficient)\n" +
                "LOCATION IMPORT/EXPORT <filename> - import/export locations to csv file\n" +
                "SCHEDULE <location_name> - list all departing and arriving flights, in order of the time they arrive/depart\n" +
                "DEPARTURES <location_name> - list all departing flights, in order of departure time\n" +
                "ARRIVALS <location_name> - list all arriving flights, in order of arrival time\n\n" +
                "TRAVEL <from> <to> [sort] [n] - list the nth possible flight route between a starting location and " +
                "destination, with a maximum of 3 stopovers. Default ordering is for shortest overall duration. If n is not " +
                "provided, display the first one in the order. If n is larger than the number of flights available, display the " +
                "last one in the ordering.\n\n" +
                "can have other orderings:\n" +
                "TRAVEL <from> <to> cost - minimum current cost\n" +
                "TRAVEL <from> <to> duration - minimum total duration\n" +
                "TRAVEL <from> <to> stopovers - minimum stopovers\n" +
                "TRAVEL <from> <to> layover - minimum layover time\n" +
                "TRAVEL <from> <to> flight_time - minimum flight time\n\n" +
                "HELP - outputs this help string.\n" +
                "EXIT - end the program.\n");
    }

    public static int convertDay(String s) {
        int cday = -1;
        for (int i = 0; i < sWeek.length; i++) {
            if (sWeek[i].substring(0, 3).equalsIgnoreCase(s.substring(0, 3))) {
                cday = i;
                break;
            }
        }
        return cday;
    }


    //flight import <filename>
    public void importFlights(String[] command) {
        try {
            if (command.length < 3) throw new FileNotFoundException();
            BufferedReader br = new BufferedReader(new FileReader(new File(command[2])));
            String line;
            int count = 0;
            int err = 0;

            while ((line = br.readLine()) != null) {
                String[] lparts = line.split(",");
                if (lparts.length < 5) continue;
                String[] dparts = lparts[0].split(" ");
                if (dparts.length < 2) continue;
                int booked = 0;

                try {
                    booked = Integer.parseInt(lparts[4]);

                } catch (NumberFormatException e) {
                    continue;
                }

                int status = addFlight(dparts[0], dparts[1], lparts[1], lparts[2], lparts[3], booked);
                if (status < 0) {
//                    System.out.println(status);
//                    System.out.println(dparts[0]+" "+dparts[1]+" "+lparts[1]+" "+lparts[2]+" "+lparts[3]+" "+booked);
                    err++;
                    continue;
                }
                count++;
            }
            br.close();
            System.out.println("Imported " + count + " flight" + (count != 1 ? "s" : "") + ".");
            if (err > 0) {
                if (err == 1) System.out.println("1 line was invalid.");
                else System.out.println(err + " lines were invalid.");
            }
            System.out.println();
        } catch (IOException e) {
            System.out.println("Error reading file.\n");
            return;
        }
    }

    //flight export <filename>
    public void exportFlights(String[] command) {
        try {
            if (command.length < 3) throw new FileNotFoundException();
            BufferedWriter br = new BufferedWriter(new FileWriter(new File(command[2])));

            Iterator<Map.Entry<Integer, Flight>> iterator = flights.entrySet().iterator();
            int count = 0;
            while (iterator.hasNext()) {
                String s = "";
                Map.Entry<Integer, Flight> entry = iterator.next();
                Flight flight = entry.getValue();
                String capacity = flight.getCapacity().split("/")[1];
                s = flight.formatTime(flight, "dep") + "," + flight.getStart() + "," + flight.getEnd() + "," + capacity + "," + flight.getNumberOfBook();
                br.write(s);
                br.newLine();
                locations.get(flight.getStart().toLowerCase()).remDeparture(flight);
                locations.get(flight.getEnd().toLowerCase()).remDeparture(flight);
                count++;
            }
            System.out.println("Exported " + count + " flights.\n");
            br.close();
        } catch (IOException e) {
            System.out.println("Error writing file.\n");
            return;
        }
    }

    //location import <filename>
    public void importLocations(String[] command) {
        try {
            if (command.length < 3) throw new FileNotFoundException();
            BufferedReader br = new BufferedReader(new FileReader(new File(command[2])));
            String line;
            int count = 0;
            int err = 0;

            while ((line = br.readLine()) != null) {
                String[] lparts = line.split(",");
                if (lparts.length < 4) continue;

                int status = addLocation(lparts[0], lparts[1], lparts[2], lparts[3]);
                if (status < 0) {
                    err++;
                    continue;
                }
                count++;
            }
            br.close();
            System.out.println("Imported " + count + " location" + (count != 1 ? "s" : "") + ".");
            if (err > 0) {
                if (err == 1) System.out.println("1 line was invalid.");
                else System.out.println(err + " lines were invalid.");
            }
            System.out.println();
        } catch (IOException e) {
            System.out.println("Error reading file.\n");
            return;
        }
    }

    //location export <filename>
    public void exportLocation(String[] command) {
        try {
            if (command.length < 3) throw new FileNotFoundException();
            BufferedWriter br = new BufferedWriter(new FileWriter(new File(command[2])));

            Iterator<Map.Entry<String, Location>> iterator = locations.entrySet().iterator();
            int count = 0;
            while (iterator.hasNext()) {
                String s = "";
                Map.Entry<String, Location> entry = iterator.next();
                Location location = entry.getValue();
                s = location.getName() + "," + location.getLatitude() + "," + location.getLongitude() + "," + location.getDemand();
                br.write(s);
                br.newLine();
                count++;
            }
            System.out.println("Exported " + count + " locations.\n");
            br.close();
        } catch (IOException e) {
            System.out.println("Error writing file.\n");
            return;
        }
    }
}
