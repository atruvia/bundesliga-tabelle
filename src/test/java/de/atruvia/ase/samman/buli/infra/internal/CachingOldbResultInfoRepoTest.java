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

import de.atruvia.ase.samman.buli.infra.internal.OldbResultInfoRepo.OldbResultInfo;

@SpringBootTest
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
class CachingOldbResultInfoRepoTest {

	@Autowired
	OldbResultInfoRepo oldbResultInfoRepo;

	@Test
	void theOldbResultInfoRepoProvidedBySpringIsTheCachingOne() {
		assertThat(oldbResultInfoRepo).isInstanceOf(CachingOldbResultInfoRepo.class);
	}

	@Nested
	@SpringBootTest(properties = CachingOldbResultInfoRepo.CACHE_TTL + "=60000")
	class LongCacheEvict {

		@MockitoBean
		@Qualifier("defaultOldbResultInfoRepo")
		OldbResultInfoRepo delegateMock;

		@Autowired
		OldbResultInfoRepo cachingRepo;

		@Test
		void testCacheHits() {
			queryResultInfosThreeTimes("bl1", "2022");
			verify(delegateMock).getResultInfos(eq("bl1"), eq("2022"));
			verifyNoMoreInteractions(delegateMock);
		}

		@Test
		void queriesAgainBecauseLeagueDiffers() {
			String season = "2022";
			queryResultInfosThreeTimes("bl1", season);
			queryResultInfosThreeTimes("bl2", season);
			verify(delegateMock).getResultInfos(eq("bl1"), eq(season));
			verify(delegateMock).getResultInfos(eq("bl2"), eq(season));
			verifyNoMoreInteractions(delegateMock);
		}

		@Test
		void queriesAgainBecauseSeasonDiffers() {
			String league = "bl1";
			queryResultInfosThreeTimes(league, "2022");
			queryResultInfosThreeTimes(league, "2023");
			verify(delegateMock).getResultInfos(eq(league), eq("2022"));
			verify(delegateMock).getResultInfos(eq(league), eq("2023"));
			verifyNoMoreInteractions(delegateMock);
		}

		private void queryResultInfosThreeTimes(String league, String season) {
			for (int i = 0; i < 3; i++) {
				cachingRepo.getResultInfos(league, season);
			}
		}

	}

	@Nested
	@SpringBootTest(properties = CachingOldbResultInfoRepo.CACHE_TTL + "=" + ShortCacheEvict.EVICT_MS)
	class ShortCacheEvict {

		static final int EVICT_MS = 1000;

		@MockitoBean
		@Qualifier("defaultOldbResultInfoRepo")
		OldbResultInfoRepo delegateMock;

		@Autowired
		OldbResultInfoRepo cachingRepo;

		String league = "bl1";
		String season = "2023";

		@Test
		void cacheGetsEvictedAfterOneSecond() throws InterruptedException {
			var first = List.of(resultInfo("A1"));
			var second = List.of(resultInfo("B1"), resultInfo("B2"));
			when(delegateMock.getResultInfos(league, season)).thenReturn(first).thenReturn(second);

			whenCachingRepoIsQueriedTheResultIs(first);
			MILLISECONDS.sleep(EVICT_MS);
			MILLISECONDS.sleep(100);
			whenCachingRepoIsQueriedTheResultIs(second);
		}

		private void whenCachingRepoIsQueriedTheResultIs(List<OldbResultInfo> resultInfos) {
			for (int i = 0; i < 3; i++) {
				assertThat(cachingRepo.getResultInfos(league, season)).isSameAs(resultInfos);
			}
		}

		private static OldbResultInfo resultInfo(String name) {
			OldbResultInfo resultInfo = new OldbResultInfo();
			resultInfo.name = name;
			return resultInfo;
		}

	}

}
