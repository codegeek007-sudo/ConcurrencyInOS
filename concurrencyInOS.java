/*
* Author: Ameya Patil
*
* Project 2
*
* */

import java.util.concurrent.Semaphore;
import java.util.Queue;
import java.util.LinkedList;

public class Project2 {
    // Semaphore for receptionist registering
    static Semaphore waitOnRecep = new Semaphore(0, true);
    // Semaphore for Patient checking in
    static Semaphore checkIn = new Semaphore(0, true);
    // Doctor's waiting room
    static Semaphore checkedIn = new Semaphore(0, true);
    // Semaphore for Patient leaving
    static Semaphore goToNurse = new Semaphore(0, true);

    // Nurse Semaphores
    static Semaphore checkInNurse = new Semaphore(0, true);
    static Semaphore readyForNurse = new Semaphore(0, true);
    static Semaphore goToDoc = new Semaphore(0, true);

    // Doctor Semaphores
    static Semaphore docReady = new Semaphore(0, true);
    static Semaphore enterDocRoom = new Semaphore(0, true);
    static Semaphore adviced = new Semaphore(0, true);
    static Semaphore adviceReceived = new Semaphore(0, true);

    // Patient enters the office waiting room
    static Semaphore mutex1 = new Semaphore(1, true);


    // Queue for patients to enqueue and dequeue from reception
	public static Queue<Integer> receptionQueue = new LinkedList<Integer>();
	public static Queue<Integer> nurseQueue = new LinkedList<Integer>();
	public static Queue<Integer> doctorQueue = new LinkedList<Integer>();
	public static Queue<Integer> symptomsQueue = new LinkedList<Integer>();
	public static Queue<Integer> advicedQueue = new LinkedList<Integer>();




    // static Semaphore registeredPatients[];
    // static Semaphore advicedPatients[];
    static boolean flag = true;

    public static void main(String[] args) {

        int numDoctors, numPatients, numNurses;

        // FIRST ARG: Doctors
        // SECOND ARG: Patients
        numDoctors = Integer.parseInt(args[0]);
        numPatients = Integer.parseInt(args[1]);

        // numDoctors = 3;
        // numPatients = 5;

        numNurses = numDoctors;
        if (numDoctors >= 4 || numPatients >= 31) {
            System.out.println("Error! Too many Doctors or too many Patients; max value is 3 doctors, 30 patients.");
            System.exit(0);
        }
        // System.out.println("CMD-L args: " + numDoctors + " " + numPatients);
        System.out.println(
                "\n Run with " + numPatients + " patients, " + numNurses + " nurses, " + numDoctors + " doctors \n");

        // Single thread for receptionist
        Thread receptionist = new Thread(new Receptionist());
        receptionist.setDaemon(true);
        receptionist.start();

        // Threads for patients
        Thread[] patient = new Thread[numPatients];
        // registeredPatients = new Semaphore[numPatients];
        // advicedPatients = new Semaphore[numPatients];
        for (int i = 0; i < numPatients; i++) {
            patient[i] = new Thread(new Patient(i));
            //registeredPatients[i] = new Semaphore(0, true);
            // advicedPatients[i] = new Semaphore(0, true);
            patient[i].setDaemon(true);
            patient[i].start();
        }

        // Threads for doctors and nurses, in same loop as they are equal
        Thread[] doctor = new Thread[numDoctors];
        Thread[] nurse = new Thread[numNurses];
        for (int i = 0; i < numDoctors; i++) {
            doctor[i] = new Thread(new Doctor(i));
            doctor[i].setDaemon(true);
            doctor[i].start();

        }

        for (int i = 0; i < numDoctors; i++) {
            nurse[i] = new Thread(new Nurse(i));
            nurse[i].setDaemon(true);
            nurse[i].start();
        }

        try {
            for (int i = 0; i < numPatients; i++) {

                patient[i].join();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        finally {
            //System.out.println("FINALLY!");
            flag = false;
        }

    }
}

class Patient extends Thread {
    public int patientCount;
    private int docID;

    Patient(int i) {
        this.patientCount = i;
    };

    public int getPatientCount() {
        return patientCount;
    }

    public void run() {
        try {
            Project2.mutex1.acquire();
            Project2.receptionQueue.add(patientCount);
            System.out.println("Patient " + patientCount + " enters waiting room, waits for receptionist");            
            Project2.mutex1.release();

            Project2.waitOnRecep.acquire();
            Project2.checkIn.release();
         
            Project2.checkedIn.acquire();
            // Thread.sleep(1);
            Project2.goToNurse.release();
            System.out.println("Patient " + patientCount + " leaves receptionist and sits in waiting room");  
            Project2.advicedQueue.add(patientCount);
            
            Project2.mutex1.acquire();
            Project2.nurseQueue.add(patientCount);
            Project2.mutex1.release();
            Project2.readyForNurse.release();
            Project2.checkInNurse.acquire();
            Project2.goToDoc.release();
            Project2.docReady.acquire();
            Project2.mutex1.acquire();
            Project2.doctorQueue.remove();
            System.out.println("Patient " + patientCount + " enter doctor " + docID + "'s office");       
            Project2.mutex1.release();
            Project2.enterDocRoom.release();
            // Project2.advicedPatients[patientCount].acquire();
            Project2.adviced.acquire();
            // Thread.sleep(1);
            docID = Project2.symptomsQueue.remove();
            System.out.println("Patient " + patientCount + " receives advice from doctor " + docID);
            Project2.adviceReceived.release();
            System.out.println("Patient " + patientCount + " leaves");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

class Receptionist extends Thread {
    private int patientCount;

    public void run() {
        try {
            while (Project2.flag) {
                Project2.waitOnRecep.release();
                Project2.checkIn.acquire();
                Project2.mutex1.acquire();
                patientCount = Project2.receptionQueue.remove();
                // Thread.sleep(1);
                System.out.println("Receptionist registers patient " + patientCount);
                Project2.checkedIn.release();
                Project2.mutex1.release();
                // Project2.registeredPatients[patientCount].release();
                Project2.goToNurse.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Doctor extends Thread {
    private int doctorCount;
    private int patientCount;

    Doctor(int i) {
        this.doctorCount = i;
    };

    public void run() {
        try {
            while (Project2.flag) {
                Project2.mutex1.acquire();
                Project2.doctorQueue.add(doctorCount);
                // patientCount = Project2.insideDocRoom.remove();
                Project2.symptomsQueue.add(patientCount);
                Project2.docReady.release();
                Project2.mutex1.release();
                Project2.enterDocRoom.acquire();
                patientCount = Project2.advicedQueue.remove();
                // System.out.println("Patient " + patientCount + " enters doctor " + doctorCount + "'s office");
                System.out.println("Doctor " + doctorCount + " listens to symptoms from patient " + patientCount);
                // Thread.sleep(1);
                // Project2.advicedPatients[patientCount].release();
                Project2.adviced.release();
                Project2.adviceReceived.acquire();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Nurse extends Thread {
    private int nurseCount;
    private int patientCount;

    Nurse(int i) {
        this.nurseCount = i;
    };

    public void run() {

        try {
            while (Project2.flag) {
                Project2.readyForNurse.acquire();
                Project2.mutex1.acquire();
                patientCount = Project2.nurseQueue.remove();
                System.out.println("Nurse " + nurseCount + " takes patient " + patientCount + " to doctor's office");
                // Project2.insideDocRoom.add(patientCount);
                Project2.mutex1.release();                            
                // Thread.sleep(1);
                Project2.checkInNurse.release();
                Project2.goToDoc.acquire();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
