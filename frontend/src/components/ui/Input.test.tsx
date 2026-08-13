import { render, screen } from '@testing-library/react';
import { Input } from './Input';

describe('Input', () => {
  it('connects label and accessible validation message', () => {
    render(<Input label="Email adresa" error="Email nije ispravan." />);

    const input = screen.getByRole('textbox', { name: 'Email adresa' });
    expect(input).toHaveAttribute('aria-invalid', 'true');
    expect(input).toHaveAccessibleDescription('Email nije ispravan.');
  });
});
