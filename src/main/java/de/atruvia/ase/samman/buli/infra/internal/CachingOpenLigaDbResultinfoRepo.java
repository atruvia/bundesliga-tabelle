package de.atruvia.ase.samman.buli.infra.internal;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@Primary
@RequiredArgsConstructor
public class CachingOpenLigaDbResultinfoRepo implements OpenLigaDbResultinfoRepo {

	public static final String CACHE_TTL = "resultInfosCacheTTL";
	private static final String CACHE_NAME = "resultInfosCache";
	private static final int ONE_HOUR_MILLIS = 60 * 60 * 1000;

	private final OpenLigaDbResultinfoRepo delegate;
	private final CacheManager cacheManager;

	@Override
	@Cacheable(CACHE_NAME)
	public List<OpenligaDbResultinfo> getResultinfos(String league, String season) {
		return delegate.getResultinfos(league, season);
	}

	@Scheduled(fixedRateString = "${" + CACHE_TTL + ":" + ONE_HOUR_MILLIS + "}")
	public void evictCacheEntries() {
		Optional.ofNullable(cacheManager.getCache(CACHE_NAME)).ifPresent(Cache::clear);
	}

}
