export function isStringValidDate(dateStr: string): boolean {
  const date = new Date(dateStr);
  return !isNaN(date.getTime());
}

export function isStringValidTimeWithoutSeconds(timeStr: string): boolean {
  const regexp = new RegExp('^[0-9][0-9]:[0-9][0-9]$');
  return regexp.test(timeStr);
}
