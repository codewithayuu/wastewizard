import { ActivityEntry, Material } from '../store/appStore';

export type ImpactMetrics = {
  items: number;
  co2eKg: number;
  waterL: number;
  energyKwh: number;
  eq?: { drivingKm?: number; phoneCharges?: number };
};

const CO2E_PER_MATERIAL: Record<Material, number> = {
  plastic: 0.15,
  paper: 0.08,
  glass: 0.2,
  metal: 0.25,
  organic: 0.05,
  'e-waste': 0.0,
  hazardous: 0.0,
};

const WATER_PER_MATERIAL_L: Record<Material, number> = {
  plastic: 1.0,
  paper: 5.0,
  glass: 0.5,
  metal: 0.8,
  organic: 2.0,
  'e-waste': 0.0,
  hazardous: 0.0,
};

const ENERGY_PER_MATERIAL_KWH: Record<Material, number> = {
  plastic: 0.03,
  paper: 0.05,
  glass: 0.02,
  metal: 0.06,
  organic: 0.01,
  'e-waste': 0.0,
  hazardous: 0.0,
};

export function computeWeeklyImpact(activity: ActivityEntry[], now = Date.now()): ImpactMetrics {
  const weekMs = 7 * 24 * 60 * 60 * 1000;
  const recent = activity.filter((a) => {
    if (a.type !== 'scan' && a.type !== 'disposed') return false;
    const t = a.when === 'Just now' ? now : new Date(a.when).getTime();
    return now - t < weekMs;
  });

  let items = 0;
  let co2e = 0;
  let water = 0;
  let energy = 0;

  for (const e of recent) {
    items += 1;
    co2e += CO2E_PER_MATERIAL[e.material] || 0;
    water += WATER_PER_MATERIAL_L[e.material] || 0;
    energy += ENERGY_PER_MATERIAL_KWH[e.material] || 0;
  }

  const drivingKm = co2e > 0 ? co2e / 0.12 : 0; // ~0.12 kg CO2 per km
  const phoneCharges = energy > 0 ? (energy * 1000) / 5 : 0; // assume 5 Wh per phone charge

  return {
    items,
    co2eKg: Number(co2e.toFixed(2)),
    waterL: Number(water.toFixed(1)),
    energyKwh: Number(energy.toFixed(2)),
    eq: { drivingKm: Number(drivingKm.toFixed(1)), phoneCharges: Math.round(phoneCharges) },
  };
}
