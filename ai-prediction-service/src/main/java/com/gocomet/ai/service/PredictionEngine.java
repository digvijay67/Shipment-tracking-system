package com.gocomet.ai.service;

import com.gocomet.ai.dto.PredictionDtos.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * AI Prediction Engine — Java formula-based ETA and risk prediction.
 *
 * Formula:  ETA_hours = (distanceKm / avgSpeedKmh) * trafficFactor * weatherFactor * carrierFactor
 * Risk     = f(daysOverdue, statusProgression, weightPenalty, distanceFactor)
 *
 * All logic is deterministic and explainable — ideal for a logistics SaaS.
 */
@Service
@Slf4j
public class PredictionEngine {

    // Average speeds by carrier tier (km/h)
    private static final Map<String, Double> CARRIER_SPEEDS = Map.of(
        "EXPRESS",          800.0,   // Air freight
        "PRIORITY",         500.0,   // Fast road/rail
        "STANDARD",         300.0,   // Standard road
        "ECONOMY",          180.0,   // Slow economy
        "GOCOMET LOGISTICS", 350.0   // Default
    );

    // Carrier reliability factor (< 1 = faster, > 1 = slower)
    private static final Map<String, Double> CARRIER_FACTORS = Map.of(
        "EXPRESS", 0.85, "PRIORITY", 0.92, "STANDARD", 1.0,
        "ECONOMY", 1.15, "GOCOMET LOGISTICS", 1.0
    );

    public PredictionResponse predict(PredictionRequest req) {
        double distanceKm = resolveDistance(req);
        double avgSpeed   = resolveSpeed(req.getCarrier());
        double trafficFactor  = computeTrafficFactor(req.getShipmentDate());
        double weatherFactor  = computeWeatherFactor(req.getOrigin(), req.getDestination());
        double carrierFactor  = CARRIER_FACTORS.getOrDefault(
                normalizeCarrier(req.getCarrier()), 1.0);
        double weightPenalty  = computeWeightPenalty(req.getWeightKg());

        // Core ETA formula
        double rawHours = (distanceKm / avgSpeed)
                * trafficFactor
                * weatherFactor
                * carrierFactor
                * weightPenalty;

        // Add handling overhead: pickup + dispatch + last-mile
        double totalHours = rawHours + 4.0 + 2.0 + estimateLastMileHours(req.getDestination());

        LocalDateTime predictedEta = LocalDateTime.now().plusHours((long) totalHours);

        // Risk scoring
        RiskAssessment risk = assessRisk(req, predictedEta, totalHours, distanceKm);

        double confidenceScore = computeConfidence(req, distanceKm);

        return PredictionResponse.builder()
                .shipmentId(req.getShipmentId())
                .predictedEtaHours(Math.round(totalHours * 10.0) / 10.0)
                .predictedEtaDate(predictedEta)
                .riskLevel(risk.getLevel())
                .riskScore(risk.getScore())
                .riskFactors(risk.getFactors())
                .confidenceScore(Math.round(confidenceScore * 100.0) / 100.0)
                .breakdown(PredictionBreakdown.builder()
                        .distanceKm(distanceKm)
                        .avgSpeedKmh(avgSpeed)
                        .trafficFactor(trafficFactor)
                        .weatherFactor(weatherFactor)
                        .carrierFactor(carrierFactor)
                        .weightPenaltyFactor(weightPenalty)
                        .rawTransitHours(Math.round(rawHours * 10.0) / 10.0)
                        .handlingHours(6.0 + estimateLastMileHours(req.getDestination()))
                        .build())
                .generatedAt(LocalDateTime.now())
                .build();
    }

    public RiskAssessment assessRisk(PredictionRequest req, LocalDateTime eta,
                                      double totalHours, double distanceKm) {
        double riskScore = 0.0;
        StringBuilder factors = new StringBuilder();

        // Factor 1: overdue check
        if (req.getExpectedDelivery() != null) {
            long overdueHours = ChronoUnit.HOURS.between(req.getExpectedDelivery(), eta);
            if (overdueHours > 48) {
                riskScore += 40.0;
                factors.append("HIGH overdue risk (+").append(overdueHours).append("h); ");
            } else if (overdueHours > 24) {
                riskScore += 25.0;
                factors.append("MODERATE overdue risk; ");
            } else if (overdueHours > 0) {
                riskScore += 10.0;
                factors.append("SLIGHT delay possible; ");
            }
        }

        // Factor 2: long distance penalty
        if (distanceKm > 5000) {
            riskScore += 20.0;
            factors.append("Long-haul route (>5000km); ");
        } else if (distanceKm > 2000) {
            riskScore += 10.0;
            factors.append("Medium-haul route; ");
        }

        // Factor 3: heavy cargo
        if (req.getWeightKg() > 500) {
            riskScore += 15.0;
            factors.append("Heavy cargo (>500kg); ");
        } else if (req.getWeightKg() > 100) {
            riskScore += 5.0;
            factors.append("Moderate weight cargo; ");
        }

        // Factor 4: weekend/holiday shipment
        if (req.getShipmentDate() != null) {
            DayOfWeek dow = req.getShipmentDate().toLocalDate().getDayOfWeek();
            if (dow == DayOfWeek.FRIDAY || dow == DayOfWeek.SATURDAY) {
                riskScore += 10.0;
                factors.append("Weekend dispatch delay risk; ");
            }
        }

        // Factor 5: economy carrier for long distance
        String carrier = normalizeCarrier(req.getCarrier());
        if ("ECONOMY".equals(carrier) && distanceKm > 1000) {
            riskScore += 15.0;
            factors.append("Economy carrier on long route; ");
        }

        // Clamp to 100
        riskScore = Math.min(riskScore, 100.0);

        String level = riskScore >= 60 ? "HIGH" : riskScore >= 30 ? "MEDIUM" : "LOW";

        return new RiskAssessment(level, Math.round(riskScore * 10.0) / 10.0,
                factors.length() > 0 ? factors.toString().trim() : "No significant risk factors detected");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private double resolveDistance(PredictionRequest req) {
        if (req.getDistanceKm() != null && req.getDistanceKm() > 0) {
            return req.getDistanceKm();
        }
        // Fallback: city-pair lookup (simplified)
        return estimateDistanceFromCities(req.getOrigin(), req.getDestination());
    }

    private double estimateDistanceFromCities(String origin, String destination) {
        // Simple hash-based pseudo-distance for demo
        int hash = Math.abs((origin + destination).hashCode() % 8000);
        return 200.0 + hash;  // 200–8200 km
    }

    private double resolveSpeed(String carrier) {
        return CARRIER_SPEEDS.getOrDefault(normalizeCarrier(carrier), 300.0);
    }

    private double computeTrafficFactor(LocalDateTime shipmentDate) {
        if (shipmentDate == null) return 1.05;
        DayOfWeek dow = shipmentDate.getDayOfWeek();
        int hour = shipmentDate.getHour();
        // Peak hours penalty
        boolean peakHour = (hour >= 8 && hour <= 10) || (hour >= 17 && hour <= 19);
        boolean weekday  = dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
        if (weekday && peakHour) return 1.20;
        if (weekday) return 1.05;
        return 0.95; // weekend = less traffic
    }

    private double computeWeatherFactor(String origin, String destination) {
        // Simplified: ports and coastal routes get slight weather penalty
        String route = (origin + destination).toLowerCase();
        if (route.contains("mumbai") || route.contains("chennai") || route.contains("kolkata")) {
            return 1.08; // Monsoon risk
        }
        if (route.contains("delhi") || route.contains("lahore")) {
            return 1.05; // Fog/smog season
        }
        return 1.0;
    }

    private double computeWeightPenalty(Double weightKg) {
        if (weightKg == null) return 1.0;
        if (weightKg > 1000) return 1.25;
        if (weightKg > 500)  return 1.12;
        if (weightKg > 100)  return 1.05;
        return 1.0;
    }

    private double estimateLastMileHours(String destination) {
        // Metro cities get faster last mile
        String dest = destination.toLowerCase();
        if (dest.contains("mumbai") || dest.contains("delhi") || dest.contains("bangalore")) {
            return 2.0;
        }
        return 4.0; // Tier-2/3 cities
    }

    private double computeConfidence(PredictionRequest req, double distanceKm) {
        double confidence = 0.95;
        if (req.getDistanceKm() == null || req.getDistanceKm() <= 0) confidence -= 0.10;
        if (req.getCarrier() == null) confidence -= 0.05;
        if (distanceKm > 5000) confidence -= 0.05;
        return Math.max(0.60, confidence);
    }

    private String normalizeCarrier(String carrier) {
        if (carrier == null) return "STANDARD";
        String upper = carrier.toUpperCase();
        for (String key : CARRIER_SPEEDS.keySet()) {
            if (upper.contains(key)) return key;
        }
        return "STANDARD";
    }

    // Inner value objects
    public record RiskAssessment(String level, double score, String factors) {
        public String getLevel()   { return level; }
        public double getScore()   { return score; }
        public String getFactors() { return factors; }
    }
}
