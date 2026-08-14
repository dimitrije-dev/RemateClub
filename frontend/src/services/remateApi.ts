import { api } from './api';

export type ClubStatus = 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED' | 'SUSPENDED';
export type CourtType = 'STANDARD' | 'PANORAMIC' | 'SINGLE';
export type BookingStatus = 'CONFIRMED' | 'COMPLETED' | 'CANCELLED';

export type Club = {
  id: string;
  ownerId: string;
  name: string;
  city: string;
  status: ClubStatus;
  createdAt: string;
  updatedAt: string;
  coverImageUrl?: string | null;
};

export type ClubImage = {
  id: string;
  clubId: string;
  url: string;
  originalFilename: string;
  contentType: 'image/jpeg' | 'image/png' | 'image/webp';
  fileSize: number;
  width: number;
  height: number;
  altText: string;
  sortOrder: number;
  cover: boolean;
  createdAt: string;
};

export type Court = {
  id: string;
  clubId: string;
  name: string;
  type: CourtType;
  active: boolean;
  hourlyPrice: number;
  createdAt: string;
  updatedAt: string;
};

export type Booking = {
  id: string;
  playerId: string;
  courtId: string;
  clubId: string;
  startAt: string;
  endAt: string;
  status: BookingStatus;
  totalPrice: number;
  currency: string;
  cancelledAt?: string;
  createdAt: string;
  updatedAt: string;
};

export type AvailabilitySlot = {
  startAt: string;
  endAt: string;
  available: boolean;
  price: number;
  currency: string;
};

export type Availability = {
  courtId: string;
  date: string;
  zoneId: string;
  slots: AvailabilitySlot[];
};

export type ClubPayload = { name: string; city: string };
export type CourtPayload = { name: string; type: CourtType; active: boolean; hourlyPrice: number };

export const remateApi = {
  async getClubs() {
    return (await api.get<Club[]>('/clubs')).data;
  },
  async getClub(clubId: string) {
    return (await api.get<Club>(`/clubs/${clubId}`)).data;
  },
  async getPublicCourts(clubId: string) {
    return (await api.get<Court[]>(`/clubs/${clubId}/courts`)).data;
  },
  async getClubImages(clubId: string) {
    return (await api.get<ClubImage[]>(`/clubs/${clubId}/images`)).data;
  },
  async getAvailability(courtId: string, date: string) {
    return (await api.get<Availability>(`/courts/${courtId}/availability`, { params: { date } })).data;
  },
  async createBooking(courtId: string, startAt: string, endAt: string) {
    return (await api.post<Booking>('/bookings', { courtId, startAt, endAt })).data;
  },
  async getMyBookings() {
    return (await api.get<Booking[]>('/bookings/me')).data;
  },
  async getBooking(bookingId: string) {
    return (await api.get<Booking>(`/bookings/${bookingId}`)).data;
  },
  async cancelBooking(bookingId: string) {
    return (await api.patch<Booking>(`/bookings/${bookingId}/cancel`)).data;
  },
  async getOwnerClubs() {
    return (await api.get<Club[]>('/owner/clubs')).data;
  },
  async createClub(payload: ClubPayload) {
    return (await api.post<Club>('/owner/clubs', payload)).data;
  },
  async updateClub(clubId: string, payload: ClubPayload) {
    return (await api.put<Club>(`/owner/clubs/${clubId}`, payload)).data;
  },
  async getOwnerClubImages(clubId: string) {
    return (await api.get<ClubImage[]>(`/owner/clubs/${clubId}/images`)).data;
  },
  async uploadClubImage(clubId: string, file: File, altText: string, cover: boolean) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('altText', altText);
    formData.append('cover', String(cover));
    return (await api.post<ClubImage>(`/owner/clubs/${clubId}/images`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })).data;
  },
  async makeClubImageCover(clubId: string, imageId: string) {
    return (await api.patch<ClubImage>(`/owner/clubs/${clubId}/images/${imageId}/cover`)).data;
  },
  async deleteClubImage(clubId: string, imageId: string) {
    await api.delete(`/owner/clubs/${clubId}/images/${imageId}`);
  },
  async getOwnerCourts(clubId: string) {
    return (await api.get<Court[]>(`/owner/clubs/${clubId}/courts`)).data;
  },
  async createCourt(clubId: string, payload: CourtPayload) {
    return (await api.post<Court>(`/owner/clubs/${clubId}/courts`, payload)).data;
  },
  async updateCourt(clubId: string, courtId: string, payload: CourtPayload) {
    return (await api.put<Court>(`/owner/clubs/${clubId}/courts/${courtId}`, payload)).data;
  },
  async getOwnerBookings() {
    return (await api.get<Booking[]>('/owner/bookings')).data;
  },
  async getPendingClubs() {
    return (await api.get<Club[]>('/admin/clubs/pending')).data;
  },
  async approveClub(clubId: string) {
    return (await api.patch<Club>(`/admin/clubs/${clubId}/approve`)).data;
  },
  async rejectClub(clubId: string) {
    return (await api.patch<Club>(`/admin/clubs/${clubId}/reject`)).data;
  },
};
