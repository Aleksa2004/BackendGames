package com.example.games_extractor.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class SearchFilters {

	private Range<BigDecimal> price;
	
	private Range<Integer> requiredAge;

    private Boolean windows;

    private Boolean mac;

    private Boolean linux;

    private List<String> genres;

    private List<String> categories;

    private Range<LocalDate> releaseDate;

    private List<String> developers;
    
    private String semanticQuery;


    public Range<BigDecimal> getPrice() {
		return price;
	}

	public void setPrice(Range<BigDecimal> price) {
		this.price = price;
	}

	public Range<Integer> getRequiredAge() {
		return requiredAge;
	}

	public void setRequiredAge(Range<Integer> requiredAge) {
		this.requiredAge = requiredAge;
	}

	public Boolean getWindows() {
        return windows;
    }

    public void setWindows(Boolean windows) {
        this.windows = windows;
    }

    public Boolean getMac() {
        return mac;
    }

    public void setMac(Boolean mac) {
        this.mac = mac;
    }

    public Boolean getLinux() {
        return linux;
    }

    public void setLinux(Boolean linux) {
        this.linux = linux;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    

    public Range<LocalDate> getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Range<LocalDate> releaseDate) {
		this.releaseDate = releaseDate;
	}

	public List<String> getDevelopers() {
        return developers;
    }

    public void setDevelopers(List<String> developers) {
        this.developers = developers;
    }

	public String getSemanticQuery() {
		return semanticQuery;
	}

	public void setSemanticQuery(String semanticQuery) {
		this.semanticQuery = semanticQuery;
	}
    
}