import { describe, expect, it } from 'vitest';
import { validAmount, transferError } from './models';
describe('Payment form validation', () => {
  it('accepts supported decimal amounts and maximum', () => { for(const v of ['0.01','12','123.45','1000000.00']) expect(validAmount(v)).toBe(true); });
  it('rejects zero, negative, excess precision, exponent and over-limit amounts', () => { for(const v of ['0','-1','1.001','1e2','1000000.01','','NaN']) expect(validAmount(v)).toBe(false); });
  it('requires distinct accounts and a meaningful description', () => { expect(transferError('a','a','1','Fee')).toMatch(/different/);expect(transferError('a','b','1',' ')).toMatch(/description/);expect(transferError('a','b','1','Fee')).toBeNull(); });
});
