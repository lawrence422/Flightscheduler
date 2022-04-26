import java.util.*;

public class Location extends FlightScheduler {
    private String name;
    private double latitude;
    private double longitude;
    private double demand;
    ArrayList<Flight> dep = new ArrayList<Flight>();
    ArrayList<Flight> arr = new ArrayList<Flight>();

    public Location(String name, double lat, double lon, double demand) {

        this.name = name;
        this.demand = demand;
        latitude = lat;
        longitude = lon;
    }

    //Implement the Haversine formula - return value in kilometres
    public static double distance(Location l1, Location l2) {
        final double rad = Math.PI / 180;
        final double earthRadius = 6371; //km
        double laD = l1.latitude - l2.latitude;
        double loD = l1.longitude - l2.longitude;
        double laDSin = Math.pow(Math.sin(laD / 2 * rad), 2);
        double loDSin = Math.pow(Math.sin((loD / 2) * rad), 2);
        double la1Cos = Math.cos(l1.latitude * rad);
        double la2Cos = Math.cos(l2.latitude * rad);
        double distance = 2 * earthRadius * Math.asin(Math.sqrt(laDSin + la1Cos * la2Cos * loDSin));

        return distance;
    }

    public void addArrival(Flight f) {
        arr.add(f);
        Collections.sort(arr, compareByTime2);
    }

    public void addDeparture(Flight f) {
        dep.add(f);
        Collections.sort(dep, compareByTime);
    }

    public void remArrival(Flight f) {
        arr.remove(f);
        Collections.sort(arr, compareByTime2);
    }

    public void remDeparture(Flight f) {
        dep.remove(f);
        Collections.sort(dep, compareByTime);
    }

    /**
     * Check to see if Flight f can depart from this location.
     * If there is a clash, the clashing flight string is returned, otherwise null is returned.
     * A conflict is determined by if any other flights are arriving or departing at this location within an hour of this flight's departure time.
     *
     * @param f The flight to check.
     * @return "Flight <id> [departing/arriving] from <name> on <clashingFlightTime>". Return null if there is no clash.
     */
    public String hasRunwayDepartureSpace(Flight f) {
        Collections.sort(dep, compareByTime);
        Collections.sort(arr, compareByTime2);

        if (dep.size() == 0 && arr.size() == 0)
            return null;

        int minute = f.getDepInt();

        for (int i = dep.size() - 1; i >= 0; i--) {
            int minutedep = dep.get(i).getDepInt();

            if (Math.abs(minute - minutedep) > 7200 && Math.abs(minute - minutedep) < 10080) {
                if (Math.min(minute, minutedep) + 10080 - Math.max(minute, minutedep) < 60) {
                    return flightclash(dep.get(i), "dep");
                }
            }

            if (Math.abs(minute - minutedep) < 60) {
                return flightclash(dep.get(i), "dep");
            }
        }

        for (int i = arr.size() - 1; i >= 0; i--) {
            int minutearr = arr.get(i).getArrInt();

            if (Math.abs(minute - minutearr) > 7200 && Math.abs(minute - minutearr) < 10080) {
                if (Math.min(minute, minutearr) + 10080 - Math.max(minute, minutearr) < 60) {
                    return flightclash(arr.get(i), "arr");
                }
            }

            if (Math.abs(minute - minutearr) < 60) {
                return flightclash(arr.get(i), "arr");
            }
        }
        return null;
    }

    /**
     * Check to see if Flight f can arrive at this location.
     * A conflict is determined by if any other flights are arriving or departing at this location within an hour of this flight's arrival time.
     *
     * @param f The flight to check.
     * @return String representing the clashing flight, or null if there is no clash. Eg. "Flight <id> [departing/arriving] from <name> on <clashingFlightTime>"
     */
    public String hasRunwayArrivalSpace(Flight f) {
        Collections.sort(dep, compareByTime);
        Collections.sort(arr, compareByTime2);
        if (dep.size() == 0 && arr.size() == 0)
            return null;
        int minute = f.getArrInt();

        for (int i = dep.size() - 1; i >= 0; i--) {
            int minutedep = dep.get(i).getDepInt();

            if (Math.abs(minute - minutedep) > 7200 && Math.abs(minute - minutedep) < 10080) {
                if (Math.min(minute, minutedep) + 10080 - Math.max(minute, minutedep) < 60) {
                    return flightclash(dep.get(i), "dep");
                }
            }

            if (Math.abs(minute - minutedep) < 60) {
                return flightclash(dep.get(i), "dep");
            }
        }

        for (int i = arr.size() - 1; i >= 0; i--) {
            int minutearr = arr.get(i).getArrInt();

            if (Math.abs(minute - minutearr) > 7200 && Math.abs(minute - minutearr) < 10080) {
                if (Math.min(minute, minutearr) + 10080 - Math.max(minute, minutearr) < 60) {
                    return flightclash(arr.get(i), "arr");
                }
            }

            if (Math.abs(minute - minutearr) < 60) {
                return flightclash(arr.get(i), "arr");
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public double getDemand() {
        return demand;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    // Flight <id> [departing/arriving] from <name> on <clashingFlightTime>
    public String flightclash(Flight f, String s) {

        if (s.equals("dep")) {
            return new String("Flight " + f.getFlightID() + " departing from " + name + " on " + f.formatTime(f, s));
        } else {
            return new String("Flight " + f.getFlightID() + " arriving at " + name + " on " + f.formatTime(f, s));
        }
    }

    public void schedule(String args) {
        Map<Integer, String> map = new TreeMap<>();
        if (args.equalsIgnoreCase("s") || args.equalsIgnoreCase("a")) {
            for (Flight flight : arr) {
                String s = "";
                s = String.format("%4d%10s   Arrival from %s", flight.getFlightID(), flight.getArr(), flight.getStart());
                map.put(flight.getArrInt(), s);
            }
        }
        if (args.equalsIgnoreCase("s") || args.equalsIgnoreCase("d")) {
            for (Flight flight : dep) {
                String s = "";
                s = String.format("%4d%10s   Departure to %s", flight.getFlightID(), flight.getDep(), flight.getEnd());
                map.put(flight.getDepInt(), s);
            }
        }
        System.out.println(getName());
        System.out.println("-------------------------------------------------------");
        System.out.println("ID   Time        Departure/Arrival to/from Location");
        System.out.println("-------------------------------------------------------");
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            System.out.println(entry.getValue());
        }
        System.out.println();
    }

    Comparator<Flight> compareByTime = new Comparator<Flight>() {
        @Override
        public int compare(Flight a1, Flight a2) {
            if (a1.getDepInt() != a2.getDepInt())
                return Double.compare(a1.getDepInt(), a2.getDepInt());
            else
                return compareStripng.compare(a1.getStart(), a1.getStart());

        }
    };

    Comparator<Flight> compareByTime2 = new Comparator<Flight>() {
        @Override
        public int compare(Flight a1, Flight a2) {
            if (a1.getArrInt() != a2.getArrInt())
                return Double.compare(a1.getArrInt(), a2.getArrInt());
            else
                return compareStripng.compare(a1.getStart(), a1.getStart());
        }
    };

    Comparator<String> compareStripng = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return 0;
        }
    };

}







