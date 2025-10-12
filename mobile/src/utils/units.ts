export type Units = 'metric' | 'imperial';

export function fmtMassKg(kg: number, units: Units) {
  if (units === 'imperial') {
    const lb = kg * 2.20462;
    return `${lb.toFixed(1)} lb`;
  }
  return `${kg.toFixed(1)} kg`;
}

export function fmtDistanceKm(km: number, units: Units) {
  if (units === 'imperial') {
    const mi = km * 0.621371;
    return `${mi.toFixed(1)} mi`;
  }
  return `${km.toFixed(1)} km`;
}

export function fmtVolumeL(l: number, units: Units) {
  if (units === 'imperial') {
    const gal = l / 3.78541;
    return `${gal.toFixed(1)} gal`;
  }
  return `${l.toFixed(0)} L`;
}

export function fmtEnergyKwh(kwh: number) {
  return `${kwh.toFixed(1)} kWh`;
}
