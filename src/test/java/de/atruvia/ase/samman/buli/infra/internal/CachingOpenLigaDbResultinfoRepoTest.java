package de.atruvia.ase.samman.buli.infra.internal;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import de.atruvia.ase.samman.buli.infra.internal.OpenLigaDbResultinfoRepo.OpenligaDbResultinfo;

@SpringBootTest
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
class CachingOpenLigaDbResultinfoRepoTest {

	@Autowired
	OpenLigaDbResultinfoRepo openLigaDbResultinfoRepo;

	@Test
	void openLigaDbResultinfoRepoProvidedBySpringIsTheCachingOne() {
		assertThat(openLigaDbResultinfoRepo).isInstanceOf(CachingOpenLigaDbResultinfoRepo.class);
	}

	@Nested
	@SpringBootTest(properties = CachingOpenLigaDbResultinfoRepo.CACHE_TTL + "=60000")
	class LongCacheEvict {

		@MockitoBean
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
	@SpringBootTest(properties = CachingOpenLigaDbResultinfoRepo.CACHE_TTL + "=" + ShortCacheEvict.EVICT_MS)
	class ShortCacheEvict {

		static final int EVICT_MS = 1000;

		@MockitoBean
		@Qualifier("defaultOpenLigaDbResultinfoRepo")
		OpenLigaDbResultinfoRepo delegateMock;

		@Autowired
		OpenLigaDbResultinfoRepo cachingRepo;

		String league = "bl1";
		String season = "2023";

		@Test
		void cacheGetsEvictedAfterOneSecond() throws InterruptedException {
			var first = List.of(resultinfo("A1"));
			var second = List.of(resultinfo("B1"), resultinfo("B2"));
			when(delegateMock.getResultinfos(league, season)).thenReturn(first).thenReturn(second);

			whenCachingRepoIsQueriedTheResultIs(first);
			MILLISECONDS.sleep(EVICT_MS);
			MILLISECONDS.sleep(100);
			whenCachingRepoIsQueriedTheResultIs(second);
		}

		private void whenCachingRepoIsQueriedTheResultIs(List<OpenligaDbResultinfo> resultinfos) {
			for (int i = 0; i < 3; i++) {
				assertThat(cachingRepo.getResultinfos(league, season)).isSameAs(resultinfos);
			}
		}

		private static OpenligaDbResultinfo resultinfo(String name) {
			OpenligaDbResultinfo resultinfo = new OpenligaDbResultinfo();
			resultinfo.name = name;
			return resultinfo;
		}

	}

}
