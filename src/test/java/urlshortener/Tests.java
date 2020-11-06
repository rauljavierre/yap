package urlshortener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import urlshortener.controllers.CsvFileController;
import urlshortener.controllers.GetInfoController;
import urlshortener.controllers.QrCodeController;
import urlshortener.controllers.UrlShortenerController;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(App.class)
@ContextConfiguration(classes = {
		App.class,
		UrlShortenerController.class,
		GetInfoController.class,
		QrCodeController.class,
		CsvFileController.class,
})
public class Tests {

	@MockBean
	private ValueOperations<String, String> valueOperations;

	@MockBean
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	private MockMvc mvc;

	private static final String HTTP_EXAMPLE_COM = "https://www.google.es/";
	private static final String NON_HTTP_EXAMPLE_COM = "non-https://www.google.es/";
	private static final String HASH = "a9efeb44";
	private static final String HASH_HTTP_EXAMPLE_COM = "http://localhost/link/"+HASH;


	@Test
	public void testIfItCanCreatesAShortURLProvidingAValidURL() throws Exception {
		given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
		this.mvc.perform(post("/link")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("url", HTTP_EXAMPLE_COM)).
				andDo(print()).
				andExpect(status().isCreated()).
				andExpect(header().string("Location", is(HASH_HTTP_EXAMPLE_COM)));
	}

	@Test
	public void testIfDoNotCreateAShortURLProvidingAnInvalidURL() throws Exception {
		given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
		this.mvc.perform(post("/link")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("url", NON_HTTP_EXAMPLE_COM)).
				andDo(print()).
				andExpect(status().isBadRequest());
	}

	@Test
	public void testIfItCanRedirectUsProvidingAValidShortURL() throws Exception {
		given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.get(HASH)).willReturn(HTTP_EXAMPLE_COM);
		this.mvc.perform(get("/r/"+HASH)).
				andDo(print()).
				andExpect(status().isTemporaryRedirect()).
				andExpect(header().string("Location", is(HTTP_EXAMPLE_COM)));
	}

	@Test
	public void testIfItDoNotRedirectUsProvidingAnInvalidShortURL() throws Exception {
		given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
		this.mvc.perform(get("/r/"+HASH)).
				andDo(print()).
				andExpect(status().isNotFound());
	}

	@Test
	public void testIfItCanReturnAQRProvidingAValidURL() throws Exception {
		given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
		this.mvc.perform(get("/qr")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("url", HTTP_EXAMPLE_COM)).
				andDo(print()).
				andExpect(status().isOk()).
				andExpect(header().string("Location", is(HTTP_EXAMPLE_COM)));
	}

	@Test
	public void testIfItDoNotReturnAQRProvidingAnInvalidURL() throws Exception {
		given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
		this.mvc.perform(get("/qr")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("url", NON_HTTP_EXAMPLE_COM)).
				andDo(print()).
				andExpect(status().isBadRequest());
	}

	@Test
	public void testIfItCanResponseWithHostInformation() throws Exception {
		given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.get("URLs")).willReturn("513512");
		given(valueOperations.get("QRs")).willReturn("124");
		given(valueOperations.get("CSVs")).willReturn("8");

		this.mvc.perform(get("/get_info")).
				andDo(print()).
				andExpect(status().isOk()).
				andExpect(jsonPath("$.NumberOfGeneratedURLs", is("513512"))).
				andExpect(jsonPath("$.NumberOfGeneratedQRs", is("124"))).
				andExpect(jsonPath("$.NumberOfGeneratedCSVs", is("8"))).
				andExpect(jsonPath("$.UsedMemory").isEmpty()).
				andExpect(jsonPath("$.Platform").isEmpty()).
				andExpect(jsonPath("$.UsageOfCPU").isEmpty()).
				andExpect(jsonPath("$.TotalMemory").isEmpty()).
				andExpect(jsonPath("$.AvailableMemory").isEmpty()).
				andExpect(jsonPath("$.NumberOfCores").isEmpty()).
				andExpect(jsonPath("$.CPUFrequency").isEmpty()).
				andExpect(jsonPath("$.BootTime").isEmpty());
	}
}
