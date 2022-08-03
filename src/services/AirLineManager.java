package services;

import Constants.Messages;
import Models.Flight;
import Models.User;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AirLineManager {
    private final FileWriterService writerService = new FileWriterService();
    private List<User> users = new ArrayList<>();
    private List<Flight> flights = new ArrayList<>();
    private User currentUser = null;

    public void signup(String[] commands) {

        if (!commands[3].equals(commands[4])){
            writerService.write("Cannot add user! The passwords are different!");
            return;
        }

        if ((commands[3].length()<8)){
            writerService.write("Cannot add user! Password too short!");
            return;
        }

        if (commands.length!=5){
            writerService.write("Error: Invalid number of arguments !");
            return;
        }


        String email = commands[1];
        String name = commands[2];
        String password = commands[3];
        String confirmationPassword = commands[4];

        // verificari
        boolean userExists = users.stream()
                .map(user -> user.getEmail())
                .collect(Collectors.toList())
                .contains(email);
        if (userExists) {
            writerService.write(Messages.getUserAlreadyExists());
            return;
        }
        if (!password.equals(confirmationPassword)) {
            writerService.write(Messages.getCannotAddUserPasswordDiff());
            return;
        }
        if (password.length() < 8) {
            writerService.write(Messages.getCannotAddUserPasswordTooShort());
            return;
        }

        User user = new User(email, name, password);
        users.add(user);
        writerService.write(Messages.getUserAdded(email));
    }

    public void login(String[] commands) {
        String email = commands[1];
        String password = commands[2];

        boolean userExists = users.stream()
                .anyMatch(user -> user.getEmail().equals(email));
        if (!userExists) {
            writerService.write(Messages.getCannotFindUser(email));
            return;
        }

        userExists = users.stream()
                .anyMatch(user -> user.getPassword().equals(password));
        if (!userExists) {
            writerService.write(Messages.getIncorrectPassword());
            return;
        }
        if (currentUser != null) {
            writerService.write(Messages.getAnotherUserIsConnected());
            return;
        }
        Optional<User> userOptional = users.stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
        if (userOptional.isPresent()) {
            currentUser = userOptional.get();
            writerService.write(Messages.getDisplayLoginUser(currentUser, LocalDateTime.now()));
        }
    }

    public void logout(String[] commands) {


        String email = commands[1];


        if (currentUser == null || !currentUser.getEmail().equals(email)) {
            writerService.write(Messages.getAnotherUserIsConnected());
            return;
        }
        currentUser = null;
        writerService.write(Messages.getLogout(email, LocalDateTime.now()));
    }

    public void displayMyFlights(String[] commands) {


        if (currentUser==null){
            writerService.write("There is no connected user!");
            return;
        }

        if (currentUser != null)
            for (int i=0;i<currentUser.getFlights().size();i++){
                writerService.write(
                        "Flight from " + currentUser.getFlights().get(i).getFrom() + " to "
                                + currentUser.getFlights().get(i).getTo() + ", date " + currentUser.getFlights().get(i).getDate() +
                                ", duration " + currentUser.getFlights().get(i).getDuration());
            }
    }

    public void addFlight(String[] commands) {


        if (currentUser==null){
            writerService.write("There is no connected user!");
            return;
        }

        boolean findFlight = false;
        for (Flight flight:flights){
            if (flight.getId()==Integer.parseInt(commands[1]))
                findFlight = true;
        }
        if (findFlight==false)
        {
            writerService.write("The flight with id "+commands[1]+" does not exist!");
            return;
        }

        for (Flight flight:currentUser.getFlights()){
            if (flight.getId()==Integer.parseInt(commands[1]) && flight.getId()==currentUser.getId()){
                writerService.write("email " + currentUser.getEmail() + " already have a ticket for flight with id "+ commands[1]);
                return;
            }
        }

        if (currentUser != null)
            for (Flight flight:flights){
                if (flight.getId()==Integer.parseInt(commands[1]))
                {
                    currentUser.addFlight(flight);
                    writerService.write(Messages.getDisplayFlight(flight));
                }
            }
    }

    public void cancelFlight(String[] commands) {


        if (currentUser==null){
            writerService.write("There is no connected user!");
            return;
        }

        List<Flight> fl = currentUser.getFlights();
        for (int f=0;f<fl.size();f++)
        {
            if (fl.get(f).getId()==Integer.parseInt(commands[1]))
            {
                writerService.write("The user with email " + currentUser.getEmail() +
                        " has successfully canceled his ticket for flight with id " + commands[1]);

                currentUser.getFlights().remove(f);

                for (int fli=0; fli<fl.size();fli++){
                    writerService.write("Flight from " + fl.get(fli).getFrom() + " to "
                            + fl.get(fli).getTo() + ", date " + fl.get(fli).getDate() +
                            ", duration " + fl.get(fli).getDuration());
                }



                return;
            }
        }
    }

    public void addFlightDetails(String[] commands) {


        if (currentUser==null){
            writerService.write("There is no connected user!");
            return;
        }

        LocalDate date = LocalDate.parse(commands[4]);

        flights.add(new Flight(
                Integer.parseInt(commands[1]),
                commands[2],
                commands[3],
                date,
                Integer.parseInt(commands[5])));
        writerService.write("Flight from " + commands[1] + " to " + commands[2] +
                ", date " + commands[3] + ", duration " + commands[5] + " successfully added!");
    }

    public void deleteFlight(String[] commands) {
        if (currentUser==null){
            writerService.write("There is no connected user!");
            return;
        }

        boolean findFlight = false;
        for (Flight flight:flights){
            if (flight.getId()==Integer.parseInt(commands[1]))
            {
                findFlight = true;
            }
        }
        if (findFlight)
        {
            for (Flight flight:flights){
                if (flight.getId()==Integer.parseInt(commands[1])){
                    flights.remove(flight);
                    writerService.write("Flight with id "+flight.getId()+" successfully deleted");
                    return;
                }
            }
        }
        else
        {
            writerService.write("The flight with id "+commands[1]+" does not exist!");
        }


        if (currentUser != null)
            for (Flight flight:flights){
                if (flight.getId()==Integer.parseInt(commands[1]))
                {
                    flights.remove(flight);
                }
            }
    }

    public void displayFlights(String[] commands) {
        for (int m=0;m<flights.size();m++){
            writerService.write("Flight from " + flights.get(m).getFrom() + " to "
                    + flights.get(m).getTo() + ", date " + flights.get(m).getDate() +
                    ", duration " + flights.get(m).getDuration());
        }
    }

    private Connection getConnection(){
        Connection connection;
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/flights","root","");
            return connection;
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public long insertFlight(int id, String from, String to,String date,int duration) {
        String query = "INSERT INTO students VALUES("+
                id+",'"+from+"','"+to+"','"+date+"',"+duration+")";
        Connection conn = getConnection();
        Statement st;
        try {
            st = conn.createStatement();
            long lastInsertedID = st.executeUpdate(query);
            return lastInsertedID;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
    public void persistFlights(String[] commands) {
        if (commands.length==1)
        {
            Connection conn = getConnection();
            Statement st;
            try {
                st = conn.createStatement();
                st.executeUpdate("TABLE flights;");
            } catch (Exception e)
            {

            }

            for (Flight flight:flights)
            {
                insertFlight(flight.getId(),flight.getFrom(),flight.getTo(),flight.getDate().toString(),flight.getDuration());
            }

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            writerService.write("The flights was successfully saved in the database at " + now + "!");
        }
        else
        {
            writerService.write("Error: Invalid number of arguments !");
        }
    }


    public void executeQuery(String query) {
        Connection conn = getConnection();
        Statement st;
        try {
            st = conn.createStatement();
            st.executeUpdate(query);
        } catch (Exception e) {
         e.printStackTrace();
        }
    }


    public void insertUser(int id, String email, String name, String password) {
        String query = "INSERT INTO students VALUES("+
                id+",'"+email+"','"+name+"','"+password+"')";
        executeQuery(query);
    }

    public void persistUsers(String[] commands) {
        if (commands.length==1)
        {
            Connection conn = getConnection();
            Statement st;
            try {
                st = conn.createStatement();
                st.executeUpdate("TABLE users;");
            } catch (Exception e)
            {

            }

            for (User user:users)
            {
                insertUser(user.getId(),user.getName(),user.getEmail(),user.getPassword());
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            writerService.write("The flights was successfully saved in the database at " + now + "!");
        }
        else
        {
            writerService.write("Error: Invalid number of arguments !");
        }
    }

    public void invalidCommand(String[] commands) {
        writerService.write("Comanda nu este valida");
    }

    public void flush() {
        writerService.flush();
    }

    public List<Flight> getFlights() {
        return flights;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public void setFlights(List<Flight> flights) {
        this.flights = flights;
    }

    public List<User> getUsers() {
        return users;
    }
}
