package com.theironyard;

import com.theironyard.entities.Hurricane;
import com.theironyard.services.HurricaneRepository;
import com.theironyard.services.UserRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HurricaneTrackerSpringApplicationTests {

	@Autowired
	HurricaneRepository hurricanes;

	@Autowired
	UserRepository users;

	@Autowired
	WebApplicationContext wac;

	MockMvc mockMvc;

	@Before
	public void before() {
		mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	@Test
	public void aTestLogin() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders.post("/login")
					.param("username","Alice")
					.param("password","pass")
		);
		Assert.assertTrue(users.findFirstByName("Alice") != null);
	}

	@Test
	public void bTestAddHurricane() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders.post("/hurricane")
					.param("hname","Matthew")
					.param("hlocation","Charleston")
					.param("hcategory","TWO")
					.param("himage","")
					.param("date",LocalDate.now().toString())
					.sessionAttr("username","Alice")
		);
		Assert.assertTrue(hurricanes.count() == 1);
	}

	@Test
	public void cEditHurricane() throws Exception {
		Hurricane h = hurricanes.findAll().iterator().next();
		mockMvc.perform(
				MockMvcRequestBuilders.post("/edit-hurricane")
					.param("id",h.id + "")
					.param("hname",h.name)
					.param("hlocation",h.location)
					.param("hcategory","FOUR")
					.param("himage",h.location)
					.param("date",h.date.toString())
					.sessionAttr("username","Alice")
		);
		h = hurricanes.findAll().iterator().next();
		Assert.assertTrue(h.category == Hurricane.Category.FOUR);
	}

	@Test
	public void dDeleteHurricane() throws Exception {
		Hurricane h = hurricanes.findAll().iterator().next();
		mockMvc.perform(
				MockMvcRequestBuilders.post("/delete-hurricane")
					.param("id",h.id + "")
					.sessionAttr("username","Alice")
		);
		Assert.assertTrue(hurricanes.count() == 0);
	}
}
