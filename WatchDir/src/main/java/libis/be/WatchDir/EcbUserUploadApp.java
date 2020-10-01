package libis.be.WatchDir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import libis.alma.api.rest.AlmaUserAPI;
import libis.user.load.*;
import libis.user.pkg.User;

public class EcbUserUploadApp {
	private static final Logger LOGGER = LogManager.getLogger(EcbUserUploadApp.class);
	private final File fileName;
	private final String inst;
	private final ecbUserExcel uExcel;

	public EcbUserUploadApp(Path file, String inst) {
		super();
		this.fileName = file.toFile();
		this.inst     = inst;
		this.uExcel   = new ecbUserExcel(this.fileName,this.inst);
	}
	
	public void doLoad() {
		int cnt =  0;
		for (User usr : this.uExcel.getUserList()) {
			cnt++;
			if (!usr.getPrimaryId().equals("Primary Identifier") && !usr.getPrimaryId().equals("ID")) {
				LOGGER.info("User Rcord "+cnt);
				System.out.println(cnt);
				User.jaxbObjectToXML(usr);
				User u = new User();
				try {
					AlmaUserAPI userApi = new AlmaUserAPI(inst);
					u = userApi.getUser(usr.getPrimaryId());
					try {
						u.updUser(usr);
						/*System.out.println("After updUser(u)");
						User.jaxbObjectToXML(u);*/
						userApi.putUser(u);
					} catch (Exception ePut) {
						LOGGER.error("Error main(): putUser() "+ePut.getMessage());
						System.out.println("Error main(): putUser() "+ePut.getMessage());
					}
				} catch (Exception eGet) {
					LOGGER.error("Error main(): getUser() "+eGet.getMessage());
					//try to create new user
					try {
						AlmaUserAPI userApi = new AlmaUserAPI(inst);
						String pId = "";
						usr.setForcePasswordChange("true");
						pId = userApi.postUser(usr);
						LOGGER.info("Info main(): postUser(): New user record pushed to Alma : "+pId);
					} catch (Exception ePost) {
						LOGGER.error("Error main(): postUser() "+ePost.getMessage());
					}
				}
			}
		}
	}
	
}
