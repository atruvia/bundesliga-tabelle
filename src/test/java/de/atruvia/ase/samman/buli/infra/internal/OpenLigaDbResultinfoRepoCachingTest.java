package de.atruvia.ase.samman.buli.infra.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

import de.atruvia.ase.samman.buli.infra.internal.OpenLigaDbResultinfoRepo.Resultinfo;

@SpringBootTest
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
class OpenLigaDbResultinfoRepoCachingTest {

	@Autowired
	OpenLigaDbResultinfoRepo openLigaDbResultinfoRepo;

	@Test
	void openLigaDbResultinfoRepoProvidedBySpringIsTheCachingOne() {
		assertThat(openLigaDbResultinfoRepo).isInstanceOf(CachingOpenLigaDbResultinfoRepo.class);
	}

	@Nested
	@SpringBootTest(properties = CachingOpenLigaDbResultinfoRepo.CACHECLEAR + "=60000")
	class LongCacheEvict {

		@MockBean
		@Qualifier("defaultOpenLigaDbResultinfoRepo")
		OpenLigaDbResultinfoRepo delegateMock;

		@Autowired
		OpenLigaDbResultinfoRepo cachingRepo;

		@Test
		void testCacheHits() {
			queryResultinfosThreeTimes("bl1", "2022");
			verify(delegateMock).getResultinfos(eq("bl1"), eq("2022"));
			verifyNoMoreInteractions(delegateMock);
		}

		@Test
		void queriesAgainBecauseLeagueDiffers() {
			String season = "2022";
			queryResultinfosThreeTimes("bl1", season);
			queryResultinfosThreeTimes("bl2", season);
			verify(delegateMock).getResultinfos(eq("bl1"), eq(season));
			verify(delegateMock).getResultinfos(eq("bl2"), eq(season));
			verifyNoMoreInteractions(delegateMock);
		}

		@Test
		void queriesAgainBecauseSeasonDiffers() {
			String league = "bl1";
			queryResultinfosThreeTimes(league, "2022");
			queryResultinfosThreeTimes(league, "2023");
			verify(delegateMock).getResultinfos(eq(league), eq("2022"));
			verify(delegateMock).getResultinfos(eq(league), eq("2023"));
			verifyNoMoreInteractions(delegateMock);
		}

		private void queryResultinfosThreeTimes(String league, String season) {
			for (int i = 0; i < 3; i++) {
				cachingRepo.getResultinfos(league, season);
			}
		}

	}

	@Nested
	@SpringBootTest(properties = CachingOpenLigaDbResultinfoRepo.CACHECLEAR + "=" + ShortCacheEvict.EVICT_MS)
	class ShortCacheEvict {

		static final int EVICT_MS = 1000;

		@MockBean
		@Qualifier("defaultOpenLigaDbResultinfoRepo")
		OpenLigaDbResultinfoRepo delegateMock;

		@Autowired
		OpenLigaDbResultinfoRepo cachingRepo;

		@Test
		void cacheGetsEvictedAfterOneSecond() throws InterruptedException {
			String league = "bl1";
			String season = "2023";

			var first = List.of(resultinfo("A1"));
			var second = List.of(resultinfo("B1"), resultinfo("B2"));
			var answers = List.of(first, second).iterator();
			when(delegateMock.getResultinfos(league, season)).thenAnswer(__ -> answers.next());

			for (int i = 0; i < 3; i++) {
				assertThat(cachingRepo.getResultinfos(league, season)).isSameAs(first);
			}
			TimeUnit.MILLISECONDS.sleep(EVICT_MS + 500);
			for (int i = 0; i < 3; i++) {
				assertThat(cachingRepo.getResultinfos(league, season)).isSameAs(second);
			}
		}

		private Resultinfo resultinfo(String name) {
			Resultinfo resultinfo = new Resultinfo();
			resultinfo.name = name;
			return resultinfo;
		}

	}

}
