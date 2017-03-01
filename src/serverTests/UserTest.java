package serverTests;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;

import org.junit.Test;

import server.Doctor;
import server.Government;
import server.Nurse;
import server.Patient;
import server.User;

public class UserTest {

	@Test
	public void testUserType() {

		// User p = new Patient("232323-2323");
		// User d = new Doctor("232323-2323");
		// User n = new Nurse("232323-2323");
		// User g = new Government("Government");
		//
		// assertEquals(p.getUserType(), "Patient");
		// assertEquals(d.getUserType(), "Doctor");
		// assertEquals(n.getUserType(), "Nurse");
		// assertEquals(g.getUserType(), "Government");

//		String ser = "123";
//		BigInteger serial = new BigInteger(ser.getBytes());
//		User p = new Patient(serial, "hash", "salt", "Per Person", "avd2");
//		User d = new Doctor(serial, "hash", "salt", "Per Person", "avd2");
//		User n = new Nurse(serial, "hash", "salt", "Per Person", "avd2");
//		User g = new Government(serial, "hash", "salt", "Per Person", "avd2");
//		assertEquals(p.getUserType(), "Patient");
//		assertEquals(d.getUserType(), "Doctor");
//		assertEquals(n.getUserType(), "Nurse");
//		assertEquals(g.getUserType(), "Government");
	}
}
