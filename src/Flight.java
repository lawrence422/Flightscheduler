import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

public class Flight extends FlightScheduler {
    private final String day;
    private final int[] departureTime;
    private final String start;
    private final String end;
    private final int capacity;
    private int numberOfBook;
    private int flightID;
    private Location startLocation;
    private Location endLocation;

    public Flight(String date1, int[] date2, Location start, Location end, int capacity, int booked, int flightID) {
        day = date1;
        departureTime = date2;
        this.start = start.getName();
        this.end = end.getName();
        this.capacity = capacity;
        numberOfBook = booked;
        this.flightID = flightID;
        startLocation = start;
        endLocation = end;
    }


    //get the number of minutes this flight takes (round to nearest whole number)
    public int getDuration() {
        double flightSpeed = 720;
        return (int) Math.round(getDistance() / flightSpeed * 60);
    }

    //implement the ticket price formula
    public double getTicketPrice() {
        double x = (double) numberOfBook / capacity;
        double y = 0;
        double price = 0;

        if (x >= 0 && x <= 0.5) {
            y = -0.4 * x + 1;
        } else if (x > 0.5 && x <= 0.7) {
            y = x + 0.3;
        } else if (x > 0.7 && x <= 1) {
            y = 0.2 / Math.PI * Math.atan(20 * x - 14) + 1;
        }
        price = y * (Location.distance(startLocation, endLocation) / 100.0) * (30 + 4 * (endLocation.getDemand() - startLocation.getDemand()));

        return price;
    }

    //book the given number of passengers onto this flight, returning the total cost
    public double book(int num) {
        double bookPrice = 0;
        if (isFull()) {
            return 0;
        } else {
            for (int i = 0; i < num; i++) {
                bookPrice += getTicketPrice();
                numberOfBook++;
                if (isFull()) {
                    break;
                }
            }
            return Math.round(bookPrice * 100.0) / 100.0;
        }
    }

    //return whether or not this flight is full
    public boolean isFull() {
        return numberOfBook >= capacity;
    }

    //get the distance of this flight in km
    public double getDistance() {
        return Location.distance(startLocation, endLocation);
    }

    //get the layover time, in minutes, between two flights
    public static int layover(Flight x, Flight y) {
        int xTime;
        int yTime;

        xTime = x.getArrInt();
        yTime = y.getDepInt();
        if (yTime < xTime)
            yTime = yTime + 10080;
        return yTime - xTime;
    }

    public String getStart() {
        String s = start.substring(0, 1).toUpperCase() + start.substring(1);
        return s;
    }

    public String getEnd() {
        String s = end.substring(0, 1).toUpperCase() + end.substring(1);
        return s;
    }

    public int getFlightID() {
        return flightID;
    }

    public String formatTime(Flight f, String s) {
        String hour;
        String minute;
        int hourInt = departureTime[0];
        int minuteInt = departureTime[1];
        String rday = this.day;

        if (s.equals("arr")) {
            hourInt = hourInt + (minuteInt + getDuration()) / 60;
            minuteInt = (minuteInt + getDuration()) % 60;
            if (hourInt >= 24) {
                rday = sWeek[(convertDay(day.substring(0, 3)) + 1) % 7];
//                rday = sWeek[(convertDay(day.substring(0,3)) + 1) % 7];
                hourInt = hourInt % 24;
            }
        }

        rday = rday.substring(0, 1).toUpperCase() + rday.substring(1);

        if (Integer.toString(hourInt).length() == 1) {
            hour = "0" + Integer.toString(hourInt);
        } else {
            hour = Integer.toString(hourInt);
        }
        if (Integer.toString(minuteInt).length() == 1) {
            minute = "0" + Integer.toString(minuteInt);
        } else {
            minute = Integer.toString(minuteInt);
        }

        return new String(rday + " " + hour + ":" + minute);
    }

    public String getArr() {
        String s = "";
        String[] s1 = formatTime(this, "arr").split(" ");
        s1[0] = s1[0].substring(0, 1).toUpperCase() + s1[0].substring(1, 3);
        s = s1[0] + " " + s1[1];
        return s;
    }

    public int getArrInt() {
        return (getDepInt() + getDuration()) % 10080;
    }


    public String getDep() {
        String s = "";
        String[] s2 = formatTime(this, "dep").split(" ");
        s2[0] = s2[0].substring(0, 1).toUpperCase() + s2[0].substring(1, 3);
        s = s2[0] + " " + s2[1];
        return s;
    }

    public int getDepInt() {
        int time;
        String[] s1 = formatTime(this, "dep").replace(":", " ").split(" ");
        int dInt = convertDay(s1[0]);
        time = dInt * 24 * 60 + Integer.parseInt(s1[1]) * 60 + Integer.parseInt(s1[2]);

        return time;
    }

    public int getNumberOfBook() {
        return numberOfBook;
    }

    public int getFull() {
        return capacity;
    }

    public String getCapacity() {
        String s = "";
        s = Integer.toString(numberOfBook) + "/" + Integer.toString(capacity);
        return s;
    }

    public void reset() {
        numberOfBook = 0;
    }

}


