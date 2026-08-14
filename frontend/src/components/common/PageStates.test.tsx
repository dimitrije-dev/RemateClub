import { fireEvent, render, screen } from '@testing-library/react';
import { EmptyState, ErrorState, LoadingState } from './PageStates';

describe('page states', () => {
  it('announces loading state', () => {
    render(<LoadingState label="Učitavanje klubova" />);
    expect(screen.getByRole('status')).toHaveTextContent('Učitavanje klubova');
  });

  it('renders empty state content', () => {
    render(<EmptyState title="Nema klubova" message="Promeni filter." />);
    expect(screen.getByRole('heading', { name: 'Nema klubova' })).toBeInTheDocument();
    expect(screen.getByText('Promeni filter.')).toBeInTheDocument();
  });

  it('offers retry from error state', () => {
    const retry = vi.fn();
    render(<ErrorState message="Servis nije dostupan." onRetry={retry} />);
    fireEvent.click(screen.getByRole('button', { name: /Pokušaj ponovo/ }));
    expect(retry).toHaveBeenCalledOnce();
  });
});
