"use client";

import {
  BarChart3,
  BedDouble,
  Bot,
  Building2,
  CalendarDays,
  CheckCircle2,
  ExternalLink,
  ImagePlus,
  KeyRound,
  Loader2,
  Map,
  MapPin,
  MessageSquare,
  Plus,
  Save,
  Search,
  Send,
  Star,
  Users,
} from "lucide-react";
import { createClient, type Session } from "@supabase/supabase-js";
import { FormEvent, useEffect, useMemo, useState } from "react";

type HotelSearchResult = {
  id: string;
  name: string;
  description: string;
  destination: string;
  address: string;
  latitude: number;
  longitude: number;
  starRating: number;
  amenities: string[];
  imageUrl?: string | null;
  lowestPricePerNight: number;
  availableRoomTypes: number;
};

type RoomOption = {
  id: string;
  roomType: string;
  capacity: number;
  totalCount: number;
  pricePerNight: number;
  minAvailableCount: number | null;
};

type HotelDetail = Omit<HotelSearchResult, "lowestPricePerNight" | "availableRoomTypes"> & {
  rooms: RoomOption[];
};

type HotelMapResponse = {
  id: string;
  name: string;
  destination: string;
  latitude: number;
  longitude: number;
};

type SearchResponse = {
  content: HotelSearchResult[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

type BookingResponse = {
  id: string;
  hotelId: string;
  roomId: string;
  userId: string;
  hotelName: string;
  roomType: string;
  checkIn: string;
  checkOut: string;
  guestCount: number;
  totalPrice: number;
  status: string;
  createdAt: string;
};

type CommentResponse = {
  commentId: string;
  hotelId: string;
  userId: string;
  overallRating: number;
  serviceRatings: Record<string, number>;
  comment: string;
  createdAt: string;
};

type CommentPageResponse = {
  content: CommentResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

type RatingSummaryResponse = {
  hotelId: string;
  totalComments: number;
  averageRating: number;
};

type RatingDistributionResponse = {
  hotelId: string;
  distribution: Record<string, number>;
};

type ServiceRatingSummary = {
  count: number;
  averageRating: number;
};

type ServiceRatingDistributionResponse = {
  hotelId: string;
  services: Record<string, ServiceRatingSummary>;
};

type ReviewData = {
  comments: CommentResponse[];
  totalComments: number;
  summary: RatingSummaryResponse | null;
  starDistribution: Record<string, number>;
  serviceDistribution: Record<string, ServiceRatingSummary>;
};

type CommentForm = {
  overallRating: string;
  cleanliness: string;
  location: string;
  staff: string;
  comfort: string;
  comment: string;
};

type AdminHotelResponse = {
  id: string;
  name: string;
  description: string;
  destination: string;
  address: string;
  latitude: number;
  longitude: number;
  starRating: number;
  amenities: string[];
  imageUrl?: string | null;
};

type AdminRoomResponse = {
  id: string;
  hotelId: string;
  roomType: string;
  capacity: number;
  totalCount: number;
  pricePerNight: number;
};

type AdminAvailabilityResponse = {
  roomId: string;
  startDate: string;
  endDate: string;
  availableCount: number;
  affectedDays: number;
};

type AiChatMessage = {
  role: "user" | "assistant";
  content: string;
};

type AiChatResponse = {
  sessionId?: string;
  reply?: string;
  message?: string;
  content?: string;
  response?: string;
};

type PageStatus = "idle" | "searching" | "loading-detail" | "booking";

const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080";
const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL || "";
const supabaseAnonKey = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY || "";
const loggedInDiscountRate = 0.85;
const supabase =
  supabaseUrl && supabaseAnonKey ? createClient(supabaseUrl, supabaseAnonKey) : null;

const initialForm = {
  destination: "Istanbul",
  checkIn: "2026-07-15",
  checkOut: "2026-07-18",
  guests: "2",
  userId: "88888888-8888-8888-8888-888888888888",
};

const initialAdminAuth = {
  token: "",
  userId: "",
};

const initialAdminHotelForm = {
  hotelId: "11111111-1111-1111-1111-111111111111",
  name: "Istanbul Bosphorus Suites",
  description: "Central hotel with Bosphorus access and business-friendly rooms.",
  destination: "Istanbul",
  address: "Besiktas, Istanbul",
  latitude: "41.0422",
  longitude: "29.0083",
  starRating: "4.7",
  amenities: "WiFi, Breakfast, Spa, Sea view",
};

const initialAdminRoomForm = {
  hotelId: "11111111-1111-1111-1111-111111111111",
  roomId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
  roomType: "Deluxe Double Room",
  capacity: "2",
  totalCount: "8",
  pricePerNight: "210",
};

const initialAvailabilityForm = {
  roomId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
  startDate: "2026-07-15",
  endDate: "2026-07-18",
  availableCount: "5",
};

const initialCommentForm: CommentForm = {
  overallRating: "5",
  cleanliness: "5",
  location: "5",
  staff: "5",
  comfort: "5",
  comment: "",
};

const initialAiMessages: AiChatMessage[] = [
  {
    role: "assistant",
    content: "Tell me where you want to stay, your dates, and guest count. I can use the project APIs to help with search and booking.",
  },
];

function formatMoney(value: number) {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    maximumFractionDigits: 0,
  }).format(value);
}

function undiscountedPrice(discountedPrice: number) {
  return discountedPrice / loggedInDiscountRate;
}

function buildUrl(path: string, params: Record<string, string>) {
  const url = new URL(path, apiBaseUrl);
  Object.entries(params).forEach(([key, value]) => url.searchParams.set(key, value));
  return url.toString();
}

function formatServiceName(value: string) {
  return value
    .replace(/[-_]/g, " ")
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

function googleMapsUrl(latitude: number, longitude: number) {
  return `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(
    `${latitude},${longitude}`,
  )}`;
}

function bearerHeaders(session: Session | null): Record<string, string> {
  return session?.access_token ? { Authorization: `Bearer ${session.access_token}` } : {};
}

async function readApiError(response: Response) {
  const text = await response.text();
  if (!text) {
    return `Request failed with status ${response.status}.`;
  }

  try {
    const payload = JSON.parse(text) as { message?: string; error?: string };
    return payload.message ?? payload.error ?? text;
  } catch {
    return text;
  }
}

function resolveImageUrl(imageUrl?: string | null) {
  if (!imageUrl) {
    return null;
  }
  if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
    return imageUrl;
  }
  return new URL(imageUrl, apiBaseUrl).toString();
}

function emptyReviewData(): ReviewData {
  return {
    comments: [],
    totalComments: 0,
    summary: null,
    starDistribution: {},
    serviceDistribution: {},
  };
}

function parseAmenities(value: string) {
  return value
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean);
}

function readAiReply(payload: AiChatResponse | string) {
  if (typeof payload === "string") {
    return payload;
  }

  return (
    payload.reply ??
    payload.message ??
    payload.content ??
    payload.response ??
    JSON.stringify(payload, null, 2)
  );
}

function mapBounds(hotels: HotelSearchResult[]) {
  const latitudes = hotels.map((hotel) => Number(hotel.latitude));
  const longitudes = hotels.map((hotel) => Number(hotel.longitude));

  return {
    minLatitude: Math.min(...latitudes),
    maxLatitude: Math.max(...latitudes),
    minLongitude: Math.min(...longitudes),
    maxLongitude: Math.max(...longitudes),
  };
}

function mapPinPosition(hotel: Pick<HotelSearchResult, "latitude" | "longitude">, bounds: ReturnType<typeof mapBounds>) {
  const latitudeRange = Math.max(0.001, bounds.maxLatitude - bounds.minLatitude);
  const longitudeRange = Math.max(0.001, bounds.maxLongitude - bounds.minLongitude);
  const left = 10 + ((Number(hotel.longitude) - bounds.minLongitude) / longitudeRange) * 80;
  const top = 90 - ((Number(hotel.latitude) - bounds.minLatitude) / latitudeRange) * 80;

  return {
    left: `${Math.min(90, Math.max(10, left))}%`,
    top: `${Math.min(90, Math.max(10, top))}%`,
  };
}

function PriceDisplay({
  className = "text-xl",
  discounted,
  price,
}: {
  className?: string;
  discounted: boolean;
  price: number;
}) {
  return (
    <div className="grid justify-items-end gap-1">
      {discounted ? (
        <p className="text-xs font-semibold text-slate-400 line-through">
          {formatMoney(undiscountedPrice(price))}
        </p>
      ) : null}
      <p className={`${className} font-bold text-slate-950`}>{formatMoney(price)}</p>
      {discounted ? (
        <span className="rounded-full bg-teal-50 px-2 py-0.5 text-xs font-bold text-teal-700">
          15% login discount
        </span>
      ) : null}
    </div>
  );
}

export default function Home() {
  const [form, setForm] = useState(initialForm);
  const [results, setResults] = useState<HotelSearchResult[]>([]);
  const [detail, setDetail] = useState<HotelDetail | null>(null);
  const [selectedHotelId, setSelectedHotelId] = useState<string | null>(null);
  const [status, setStatus] = useState<PageStatus>("idle");
  const [error, setError] = useState<string | null>(null);
  const [bookingError, setBookingError] = useState<string | null>(null);
  const [booking, setBooking] = useState<BookingResponse | null>(null);
  const [bookingRoomId, setBookingRoomId] = useState<string | null>(null);
  const [reviews, setReviews] = useState<ReviewData>(emptyReviewData);
  const [reviewsLoading, setReviewsLoading] = useState(false);
  const [reviewsError, setReviewsError] = useState<string | null>(null);
  const [commentForm, setCommentForm] = useState<CommentForm>(initialCommentForm);
  const [commentBusy, setCommentBusy] = useState(false);
  const [commentError, setCommentError] = useState<string | null>(null);
  const [commentMessage, setCommentMessage] = useState<string | null>(null);
  const [searchReviewSummaries, setSearchReviewSummaries] = useState<Record<string, RatingSummaryResponse>>({});
  const [mapHotel, setMapHotel] = useState<HotelMapResponse | null>(null);
  const [mapLoading, setMapLoading] = useState(false);
  const [mapError, setMapError] = useState<string | null>(null);
  const [totalElements, setTotalElements] = useState(0);
  const [adminAuth, setAdminAuth] = useState(initialAdminAuth);
  const [adminHotelForm, setAdminHotelForm] = useState(initialAdminHotelForm);
  const [adminImageFile, setAdminImageFile] = useState<File | null>(null);
  const [adminImageInputKey, setAdminImageInputKey] = useState(0);
  const [adminRoomForm, setAdminRoomForm] = useState(initialAdminRoomForm);
  const [availabilityForm, setAvailabilityForm] = useState(initialAvailabilityForm);
  const [adminBusy, setAdminBusy] = useState<string | null>(null);
  const [adminMessage, setAdminMessage] = useState<string | null>(null);
  const [adminError, setAdminError] = useState<string | null>(null);
  const [aiSessionId, setAiSessionId] = useState("demo-session");
  const [aiInput, setAiInput] = useState("Find me a hotel in Istanbul from 2026-07-15 to 2026-07-18 for 2 guests.");
  const [aiMessages, setAiMessages] = useState<AiChatMessage[]>(initialAiMessages);
  const [aiLoading, setAiLoading] = useState(false);
  const [aiError, setAiError] = useState<string | null>(null);
  const [authMode, setAuthMode] = useState<"login" | "register">("login");
  const [authEmail, setAuthEmail] = useState("");
  const [authPassword, setAuthPassword] = useState("");
  const [authSession, setAuthSession] = useState<Session | null>(null);
  const [authLoading, setAuthLoading] = useState(false);
  const [authMessage, setAuthMessage] = useState<string | null>(null);
  const [authError, setAuthError] = useState<string | null>(null);

  const stayLabel = useMemo(
    () => `${form.checkIn} to ${form.checkOut} / ${form.guests} guests`,
    [form.checkIn, form.checkOut, form.guests],
  );
  const bounds = useMemo(() => (results.length > 0 ? mapBounds(results) : null), [results]);
  const selectedResult = useMemo(
    () => results.find((hotel) => hotel.id === selectedHotelId) ?? null,
    [results, selectedHotelId],
  );
  const activeUserId = authSession?.user.id ?? form.userId;
  const canCommentOnSelectedHotel = Boolean(
    detail && booking?.hotelId === detail.id && booking.status === "CONFIRMED",
  );

  useEffect(() => {
    if (!supabase) {
      return;
    }

    supabase.auth.getSession().then(({ data }) => {
      const session = data.session;
      setAuthSession(session);
      if (session?.user.id) {
        setForm((current) => ({ ...current, userId: session.user.id }));
      }
    });

    const {
      data: { subscription },
    } = supabase.auth.onAuthStateChange((_event, session) => {
      setAuthSession(session);
      if (session?.user.id) {
        setForm((current) => ({ ...current, userId: session.user.id }));
      }
    });

    return () => subscription.unsubscribe();
  }, []);

  async function submitAuth(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!supabase) {
      setAuthError("Supabase frontend env values are not configured.");
      return;
    }

    setAuthLoading(true);
    setAuthError(null);
    setAuthMessage(null);

    try {
      const credentials = {
        email: authEmail.trim(),
        password: authPassword,
      };
      const { data, error: requestError } =
        authMode === "register"
          ? await supabase.auth.signUp(credentials)
          : await supabase.auth.signInWithPassword(credentials);

      if (requestError) {
        throw requestError;
      }

      const session = data.session;
      if (session?.user.id) {
        setForm((current) => ({ ...current, userId: session.user.id }));
      }

      setAuthPassword("");
      setAuthMessage(
        authMode === "register" && !session
          ? "Registration created. Check your email if confirmation is enabled."
          : "Signed in. Search prices now include the authenticated-user discount when the token is valid.",
      );
    } catch (caught) {
      setAuthError(caught instanceof Error ? caught.message : "Authentication failed.");
    } finally {
      setAuthLoading(false);
    }
  }

  async function signOut() {
    if (!supabase) {
      return;
    }

    setAuthLoading(true);
    setAuthError(null);
    setAuthMessage(null);

    try {
      const { error: requestError } = await supabase.auth.signOut();
      if (requestError) {
        throw requestError;
      }
      setAuthSession(null);
      setForm((current) => ({ ...current, userId: initialForm.userId }));
      setAuthMessage("Signed out. Booking is back to the demo user id.");
    } catch (caught) {
      setAuthError(caught instanceof Error ? caught.message : "Sign out failed.");
    } finally {
      setAuthLoading(false);
    }
  }

  async function searchHotels(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setStatus("searching");
    setError(null);
    setBookingError(null);
    setBooking(null);
    setDetail(null);
    setSelectedHotelId(null);
    setReviews(emptyReviewData());
    setReviewsError(null);
    setCommentError(null);
    setCommentMessage(null);
    setMapHotel(null);
    setMapError(null);
    setSearchReviewSummaries({});

    try {
      const response = await fetch(
        buildUrl("/api/v1/hotels/search", {
          destination: form.destination,
          checkIn: form.checkIn,
          checkOut: form.checkOut,
          guests: form.guests,
          page: "0",
          size: "10",
        }),
        {
          headers: bearerHeaders(authSession),
        },
      );

      if (!response.ok) {
        throw new Error(await response.text());
      }

      const data = (await response.json()) as SearchResponse;
      setResults(data.content);
      setTotalElements(data.totalElements);
      setSearchReviewSummaries(await loadSearchReviewSummaries(data.content));

      if (data.content[0]) {
        await loadHotelDetail(data.content[0].id);
      }
    } catch (caught) {
      setResults([]);
      setTotalElements(0);
      setError(caught instanceof Error ? caught.message : "Search failed.");
    } finally {
      setStatus("idle");
    }
  }

  async function loadHotelDetail(hotelId: string, resetBooking = true) {
    setStatus("loading-detail");
    setError(null);
    setBookingError(null);
    if (resetBooking) {
      setBooking(null);
      setCommentForm(initialCommentForm);
    }
    setSelectedHotelId(hotelId);
    setReviewsError(null);
    setCommentError(null);
    setCommentMessage(null);
    setMapError(null);

    try {
      const [detailResponse, reviewData, mapData] = await Promise.all([
        fetch(
          buildUrl(`/api/v1/hotels/${hotelId}`, {
            checkIn: form.checkIn,
            checkOut: form.checkOut,
            guests: form.guests,
          }),
          {
            headers: bearerHeaders(authSession),
          },
        ),
        loadReviewData(hotelId),
        loadHotelMap(hotelId),
      ]);

      if (!detailResponse.ok) {
        throw new Error(await detailResponse.text());
      }

      setDetail((await detailResponse.json()) as HotelDetail);
      setReviews(reviewData);
      setMapHotel(mapData);
    } catch (caught) {
      setDetail(null);
      setError(caught instanceof Error ? caught.message : "Hotel detail failed.");
    } finally {
      setStatus("idle");
    }
  }

  async function loadReviewData(hotelId: string) {
    setReviewsLoading(true);
    try {
      const [commentsResponse, summaryResponse, distributionResponse, serviceResponse] =
        await Promise.all([
          fetch(buildUrl(`/api/v1/comments/hotel/${hotelId}`, { page: "0", size: "5" })),
          fetch(`${apiBaseUrl}/api/v1/comments/hotel/${hotelId}/summary`),
          fetch(`${apiBaseUrl}/api/v1/comments/hotel/${hotelId}/distribution`),
          fetch(`${apiBaseUrl}/api/v1/comments/hotel/${hotelId}/service-distribution`),
        ]);

      const failedResponse = [
        commentsResponse,
        summaryResponse,
        distributionResponse,
        serviceResponse,
      ].find((response) => !response.ok);

      if (failedResponse) {
        throw new Error(await readApiError(failedResponse));
      }

      const comments = (await commentsResponse.json()) as CommentPageResponse;
      const summary = (await summaryResponse.json()) as RatingSummaryResponse;
      const distribution = (await distributionResponse.json()) as RatingDistributionResponse;
      const serviceDistribution = (await serviceResponse.json()) as ServiceRatingDistributionResponse;

      return {
        comments: comments.content,
        totalComments: comments.totalElements,
        summary,
        starDistribution: distribution.distribution,
        serviceDistribution: serviceDistribution.services,
      };
    } catch (caught) {
      setReviewsError(caught instanceof Error ? caught.message : "Review data failed.");
      return emptyReviewData();
    } finally {
      setReviewsLoading(false);
    }
  }

  async function loadSearchReviewSummaries(hotels: HotelSearchResult[]) {
    const entries = await Promise.all(
      hotels.map(async (hotel) => {
        try {
          const response = await fetch(`${apiBaseUrl}/api/v1/comments/hotel/${hotel.id}/summary`);
          if (!response.ok) {
            throw new Error(await readApiError(response));
          }
          return [hotel.id, (await response.json()) as RatingSummaryResponse] as const;
        } catch {
          return [hotel.id, { hotelId: hotel.id, totalComments: 0, averageRating: 0 }] as const;
        }
      }),
    );

    return Object.fromEntries(entries);
  }

  async function loadHotelMap(hotelId: string) {
    setMapLoading(true);
    try {
      const response = await fetch(`${apiBaseUrl}/api/v1/hotels/${hotelId}/map`);

      if (!response.ok) {
        throw new Error(await response.text());
      }

      return (await response.json()) as HotelMapResponse;
    } catch (caught) {
      setMapError(caught instanceof Error ? caught.message : "Map data failed.");
      return null;
    } finally {
      setMapLoading(false);
    }
  }

  async function createComment(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!detail) {
      return;
    }

    setCommentBusy(true);
    setCommentError(null);
    setCommentMessage(null);

    try {
      if (!canCommentOnSelectedHotel) {
        throw new Error("You need a confirmed booking for this hotel before adding a comment.");
      }

      const response = await fetch(`${apiBaseUrl}/api/v1/comments`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          hotelId: detail.id,
          userId: activeUserId,
          overallRating: Number(commentForm.overallRating),
          serviceRatings: {
            cleanliness: Number(commentForm.cleanliness),
            location: Number(commentForm.location),
            staff: Number(commentForm.staff),
            comfort: Number(commentForm.comfort),
          },
          comment: commentForm.comment.trim(),
        }),
      });

      if (!response.ok) {
        throw new Error(await readApiError(response));
      }

      setCommentForm(initialCommentForm);
      setCommentMessage("Your comment was added.");

      const updatedReviews = await loadReviewData(detail.id);
      setReviews(updatedReviews);
      if (updatedReviews.summary) {
        setSearchReviewSummaries((current) => ({
          ...current,
          [detail.id]: updatedReviews.summary as RatingSummaryResponse,
        }));
      }
    } catch (caught) {
      setCommentError(caught instanceof Error ? caught.message : "Comment creation failed.");
    } finally {
      setCommentBusy(false);
    }
  }

  async function createBooking(room: RoomOption) {
    setStatus("booking");
    setBookingRoomId(room.id);
    setBooking(null);
    setBookingError(null);
    setError(null);

    try {
      const response = await fetch(`${apiBaseUrl}/api/v1/bookings`, {
        method: "POST",
        headers: {
          ...bearerHeaders(authSession),
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          roomId: room.id,
          userId: authSession?.user.id ?? form.userId,
          checkIn: form.checkIn,
          checkOut: form.checkOut,
          guestCount: Number(form.guests),
        }),
      });

      if (!response.ok) {
        throw new Error(await response.text());
      }

      setBooking((await response.json()) as BookingResponse);
      if (selectedHotelId) {
        await loadHotelDetail(selectedHotelId, false);
      }
    } catch (caught) {
      setBookingError(caught instanceof Error ? caught.message : "Booking failed.");
    } finally {
      setStatus("idle");
      setBookingRoomId(null);
    }
  }

  function adminHeaders(includeJsonContentType = true) {
    if (!adminAuth.token.trim()) {
      throw new Error("Admin Authorization token is required.");
    }

    const authorization = adminAuth.token.trim().startsWith("Bearer ")
      ? adminAuth.token.trim()
      : `Bearer ${adminAuth.token.trim()}`;

    const headers: Record<string, string> = { Authorization: authorization };

    if (includeJsonContentType) {
      headers["Content-Type"] = "application/json";
    }

    if (adminAuth.userId.trim()) {
      headers["X-User-Id"] = adminAuth.userId.trim();
    }

    return headers;
  }

  async function adminRequest<T>(action: string, path: string, method: "POST" | "PUT", body: unknown) {
    setAdminBusy(action);
    setAdminError(null);
    setAdminMessage(null);

    try {
      const response = await fetch(`${apiBaseUrl}${path}`, {
        method,
        headers: adminHeaders(),
        body: JSON.stringify(body),
      });

      if (!response.ok) {
        throw new Error(await response.text());
      }

      return (await response.json()) as T;
    } finally {
      setAdminBusy(null);
    }
  }

  async function uploadAdminHotelImage() {
    setAdminBusy("upload-image");
    setAdminError(null);
    setAdminMessage(null);

    try {
      if (!adminHotelForm.hotelId.trim()) {
        throw new Error("Hotel id is required before uploading an image.");
      }
      if (!adminImageFile) {
        throw new Error("Choose an image file first.");
      }

      const formData = new FormData();
      formData.append("image", adminImageFile);

      const response = await fetch(`${apiBaseUrl}/api/v1/admin/hotels/${adminHotelForm.hotelId}/image`, {
        method: "POST",
        headers: adminHeaders(false),
        body: formData,
      });

      if (!response.ok) {
        throw new Error(await response.text());
      }

      const hotel = (await response.json()) as AdminHotelResponse;
      setAdminImageFile(null);
      setAdminImageInputKey((key) => key + 1);
      setAdminMessage(`Uploaded image for ${hotel.name}.`);

      if (selectedHotelId === hotel.id) {
        await loadHotelDetail(hotel.id, false);
      }
    } catch (caught) {
      setAdminError(caught instanceof Error ? caught.message : "Hotel image upload failed.");
    } finally {
      setAdminBusy(null);
    }
  }

  async function createAdminHotel() {
    try {
      const hotel = await adminRequest<AdminHotelResponse>("create-hotel", "/api/v1/admin/hotels", "POST", {
        name: adminHotelForm.name,
        description: adminHotelForm.description,
        destination: adminHotelForm.destination,
        address: adminHotelForm.address,
        latitude: Number(adminHotelForm.latitude),
        longitude: Number(adminHotelForm.longitude),
        starRating: Number(adminHotelForm.starRating),
        amenities: parseAmenities(adminHotelForm.amenities),
      });

      setAdminHotelForm({ ...adminHotelForm, hotelId: hotel.id });
      setAdminRoomForm({ ...adminRoomForm, hotelId: hotel.id });
      setAdminMessage(`Created hotel ${hotel.name} (${hotel.id}).`);
    } catch (caught) {
      setAdminError(caught instanceof Error ? caught.message : "Hotel creation failed.");
    }
  }

  async function updateAdminHotel() {
    try {
      const hotel = await adminRequest<AdminHotelResponse>(
        "update-hotel",
        `/api/v1/admin/hotels/${adminHotelForm.hotelId}`,
        "PUT",
        {
          name: adminHotelForm.name,
          description: adminHotelForm.description,
          destination: adminHotelForm.destination,
          address: adminHotelForm.address,
          latitude: Number(adminHotelForm.latitude),
          longitude: Number(adminHotelForm.longitude),
          starRating: Number(adminHotelForm.starRating),
          amenities: parseAmenities(adminHotelForm.amenities),
        },
      );

      setAdminMessage(`Updated hotel ${hotel.name}.`);
    } catch (caught) {
      setAdminError(caught instanceof Error ? caught.message : "Hotel update failed.");
    }
  }

  async function createAdminRoom() {
    try {
      const room = await adminRequest<AdminRoomResponse>(
        "create-room",
        `/api/v1/admin/hotels/${adminRoomForm.hotelId}/rooms`,
        "POST",
        {
          roomType: adminRoomForm.roomType,
          capacity: Number(adminRoomForm.capacity),
          totalCount: Number(adminRoomForm.totalCount),
          pricePerNight: Number(adminRoomForm.pricePerNight),
        },
      );

      setAdminRoomForm({ ...adminRoomForm, roomId: room.id });
      setAvailabilityForm({ ...availabilityForm, roomId: room.id });
      setAdminMessage(`Created room ${room.roomType} (${room.id}).`);
    } catch (caught) {
      setAdminError(caught instanceof Error ? caught.message : "Room creation failed.");
    }
  }

  async function updateAdminRoom() {
    try {
      const room = await adminRequest<AdminRoomResponse>(
        "update-room",
        `/api/v1/admin/rooms/${adminRoomForm.roomId}`,
        "PUT",
        {
          roomType: adminRoomForm.roomType,
          capacity: Number(adminRoomForm.capacity),
          totalCount: Number(adminRoomForm.totalCount),
          pricePerNight: Number(adminRoomForm.pricePerNight),
        },
      );

      setAdminMessage(`Updated room ${room.roomType}.`);
    } catch (caught) {
      setAdminError(caught instanceof Error ? caught.message : "Room update failed.");
    }
  }

  async function upsertAdminAvailability() {
    try {
      const availability = await adminRequest<AdminAvailabilityResponse>(
        "availability",
        `/api/v1/admin/rooms/${availabilityForm.roomId}/availability`,
        "POST",
        {
          startDate: availabilityForm.startDate,
          endDate: availabilityForm.endDate,
          availableCount: Number(availabilityForm.availableCount),
        },
      );

      setAdminMessage(
        `Updated ${availability.affectedDays} availability days for room ${availability.roomId}.`,
      );
    } catch (caught) {
      setAdminError(caught instanceof Error ? caught.message : "Availability update failed.");
    }
  }

  async function sendAiMessage(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const message = aiInput.trim();
    if (!message) {
      return;
    }

    setAiLoading(true);
    setAiError(null);
    setAiInput("");
    setAiMessages((current) => [...current, { role: "user", content: message }]);

    try {
      const response = await fetch(`${apiBaseUrl}/api/v1/ai/chat`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          sessionId: aiSessionId,
          message,
        }),
      });

      if (!response.ok) {
        throw new Error(await response.text());
      }

      const contentType = response.headers.get("content-type") ?? "";
      const payload = contentType.includes("application/json")
        ? ((await response.json()) as AiChatResponse)
        : await response.text();

      if (typeof payload !== "string" && payload.sessionId) {
        setAiSessionId(payload.sessionId);
      }

      setAiMessages((current) => [
        ...current,
        { role: "assistant", content: readAiReply(payload) },
      ]);
    } catch (caught) {
      setAiError(caught instanceof Error ? caught.message : "AI chat failed.");
      setAiMessages((current) => [
        ...current,
        {
          role: "assistant",
          content: "The AI Agent API is not available yet. Start ai-agent-service and API Gateway, then try again.",
        },
      ]);
    } finally {
      setAiLoading(false);
    }
  }

  const isBusy = status !== "idle";
  const ratingFromSummary = (summary?: RatingSummaryResponse | null) =>
    summary && summary.totalComments > 0 ? summary.averageRating : null;

  return (
    <main className="min-h-screen overflow-hidden text-slate-950">
      <section className="border-b border-slate-200/70 bg-white/80 backdrop-blur">
        <div className="mx-auto grid max-w-7xl gap-8 px-5 py-8 sm:px-8 lg:grid-cols-[0.72fr_1.28fr] lg:px-10 lg:py-10">
          <div>
            <p className="text-xs font-bold uppercase tracking-wide text-teal-700">
              SE4458 Hotel Booking
            </p>
            <h1 className="mt-3 text-4xl font-bold leading-tight text-slate-950 sm:text-5xl">
              Hotel search
            </h1>
            <p className="mt-3 max-w-xl text-sm leading-6 text-slate-600">
              API Gateway: <span className="font-semibold text-slate-900">{apiBaseUrl}</span>
            </p>
          </div>

          <div className="grid gap-4">
            <form
              className="grid w-full max-w-[calc(100vw-2.5rem)] gap-3 overflow-hidden rounded-lg border border-slate-200/80 bg-white/90 p-4 shadow-[0_18px_45px_rgba(15,23,42,0.08)] ring-1 ring-white/70 sm:max-w-none lg:grid-cols-[1fr_0.9fr_0.9fr_0.55fr_auto]"
              onSubmit={searchHotels}
            >
            <label className="grid min-w-0 gap-1 text-xs font-bold uppercase tracking-wide text-slate-600">
              Destination
              <span className="relative block min-w-0">
                <MapPin className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-teal-700" />
                <input
                  className="h-11 w-full min-w-0 rounded-md border border-slate-200 bg-white/95 pl-9 pr-3 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                  onChange={(event) => setForm({ ...form, destination: event.target.value })}
                  required
                  value={form.destination}
                />
              </span>
            </label>

            <label className="grid min-w-0 gap-1 text-xs font-bold uppercase tracking-wide text-slate-600">
              Check-in
              <span className="relative block min-w-0">
                <CalendarDays className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-teal-700" />
                <input
                  className="h-11 w-full min-w-0 rounded-md border border-slate-200 bg-white/95 pl-9 pr-3 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                  onChange={(event) => setForm({ ...form, checkIn: event.target.value })}
                  required
                  type="date"
                  value={form.checkIn}
                />
              </span>
            </label>

            <label className="grid min-w-0 gap-1 text-xs font-bold uppercase tracking-wide text-slate-600">
              Check-out
              <span className="relative block min-w-0">
                <CalendarDays className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-teal-700" />
                <input
                  className="h-11 w-full min-w-0 rounded-md border border-slate-200 bg-white/95 pl-9 pr-3 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                  onChange={(event) => setForm({ ...form, checkOut: event.target.value })}
                  required
                  type="date"
                  value={form.checkOut}
                />
              </span>
            </label>

            <label className="grid min-w-0 gap-1 text-xs font-bold uppercase tracking-wide text-slate-600">
              Guests
              <span className="relative block min-w-0">
                <Users className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-teal-700" />
                <input
                  className="h-11 w-full min-w-0 rounded-md border border-slate-200 bg-white/95 pl-9 pr-3 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                  min="1"
                  onChange={(event) => setForm({ ...form, guests: event.target.value })}
                  required
                  type="number"
                  value={form.guests}
                />
              </span>
            </label>

            <button
              className="mt-5 inline-flex h-11 items-center justify-center gap-2 rounded-md bg-slate-950 px-4 text-sm font-bold text-white shadow-lg shadow-slate-950/15 transition hover:-translate-y-0.5 hover:bg-teal-800 hover:shadow-teal-900/20 disabled:cursor-not-allowed disabled:opacity-60 disabled:hover:translate-y-0 lg:mt-5"
              disabled={isBusy}
              type="submit"
            >
              {status === "searching" ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                <Search className="h-4 w-4" />
              )}
              Search
            </button>

            <label className="grid min-w-0 gap-1 text-xs font-bold uppercase tracking-wide text-slate-600 lg:col-span-5">
              {authSession ? "Authenticated user id" : "Demo user id"}
              <input
                className="h-11 w-full min-w-0 rounded-md border border-slate-200 bg-white/95 px-3 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100 disabled:bg-slate-100 disabled:text-slate-500"
                disabled={Boolean(authSession)}
                onChange={(event) => setForm({ ...form, userId: event.target.value })}
                required
                value={form.userId}
              />
            </label>
            </form>

            <section className="w-full max-w-[calc(100vw-2.5rem)] overflow-hidden rounded-lg border border-slate-200/80 bg-white/90 p-4 shadow-[0_14px_35px_rgba(15,23,42,0.06)] ring-1 ring-white/70 sm:max-w-none">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <p className="text-xs font-bold uppercase tracking-wide text-teal-700">
                    Supabase Auth
                  </p>
                  <h2 className="mt-1 flex items-center gap-2 text-base font-bold">
                    <KeyRound className="h-4 w-4 text-teal-700" />
                    User account
                  </h2>
                </div>
                {authSession ? (
                  <button
                    className="inline-flex h-10 items-center justify-center rounded-md border border-slate-200 bg-white px-3 text-sm font-bold text-slate-700 transition hover:-translate-y-0.5 hover:border-teal-500 hover:text-teal-700 hover:shadow-sm disabled:cursor-not-allowed disabled:opacity-60 disabled:hover:translate-y-0"
                    disabled={authLoading}
                    onClick={signOut}
                    type="button"
                  >
                    Sign out
                  </button>
                ) : (
                  <div className="grid w-full grid-cols-2 overflow-hidden rounded-md border border-slate-200 bg-white p-1 text-sm font-bold shadow-inner sm:w-auto">
                    <button
                      className={`rounded px-3 py-2 transition ${authMode === "login" ? "bg-slate-950 text-white shadow-sm" : "bg-white text-slate-600 hover:bg-slate-50"}`}
                      onClick={() => setAuthMode("login")}
                      type="button"
                    >
                      Login
                    </button>
                    <button
                      className={`rounded px-3 py-2 transition ${authMode === "register" ? "bg-slate-950 text-white shadow-sm" : "bg-white text-slate-600 hover:bg-slate-50"}`}
                      onClick={() => setAuthMode("register")}
                      type="button"
                    >
                      Register
                    </button>
                  </div>
                )}
              </div>

              {!supabase ? (
                <div className="mt-4 rounded-md border border-amber-200 bg-amber-50 p-3 text-sm text-amber-800">
                  NEXT_PUBLIC_SUPABASE_URL and NEXT_PUBLIC_SUPABASE_ANON_KEY are required for login.
                </div>
              ) : null}

              {authSession ? (
                <div className="mt-4 grid gap-2 rounded-md border border-teal-200/70 bg-teal-50/80 p-3 text-sm shadow-sm">
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <span className="font-semibold text-teal-900">{authSession.user.email}</span>
                    <span className="rounded-full bg-white px-2 py-1 text-xs font-bold text-teal-700">
                      Signed in
                    </span>
                  </div>
                  <p className="font-mono text-xs text-slate-600">{authSession.user.id}</p>
                </div>
              ) : (
                <form className="mt-4 grid gap-3 sm:grid-cols-[1fr_1fr_auto]" onSubmit={submitAuth}>
                  <label className="grid gap-1 text-xs font-bold uppercase tracking-wide text-slate-600">
                    Email
                    <input
                      className="h-10 rounded-md border border-slate-200 bg-white/95 px-3 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                      disabled={!supabase || authLoading}
                      onChange={(event) => setAuthEmail(event.target.value)}
                      required
                      type="email"
                      value={authEmail}
                    />
                  </label>
                  <label className="grid gap-1 text-xs font-bold uppercase tracking-wide text-slate-600">
                    Password
                    <input
                      className="h-10 rounded-md border border-slate-200 bg-white/95 px-3 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                      disabled={!supabase || authLoading}
                      minLength={6}
                      onChange={(event) => setAuthPassword(event.target.value)}
                      required
                      type="password"
                      value={authPassword}
                    />
                  </label>
                  <button
                    className="mt-5 inline-flex h-10 items-center justify-center gap-2 rounded-md bg-teal-700 px-4 text-sm font-bold text-white shadow-md shadow-teal-900/15 transition hover:-translate-y-0.5 hover:bg-slate-950 disabled:cursor-not-allowed disabled:opacity-60 disabled:hover:translate-y-0 sm:mt-5"
                    disabled={!supabase || authLoading}
                    type="submit"
                  >
                    {authLoading ? <Loader2 className="h-4 w-4 animate-spin" /> : <KeyRound className="h-4 w-4" />}
                    {authMode === "login" ? "Login" : "Register"}
                  </button>
                </form>
              )}

              {authMessage ? (
                <div className="mt-3 rounded-md border border-teal-100 bg-teal-50 p-3 text-sm text-teal-800">
                  {authMessage}
                </div>
              ) : null}

              {authError ? (
                <div className="mt-3 rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700">
                  {authError}
                </div>
              ) : null}
            </section>
          </div>
        </div>
      </section>

      <section className="mx-auto grid max-w-7xl gap-5 px-5 py-8 sm:px-8 lg:grid-cols-[0.92fr_1.08fr] lg:px-10">
        <div className="min-h-[32rem] rounded-lg border border-slate-200/80 bg-white/95 shadow-[0_18px_45px_rgba(15,23,42,0.08)]">
          <div className="flex items-center justify-between gap-3 border-b border-slate-200/80 px-4 py-4">
            <div>
              <h2 className="text-base font-bold">Available hotels</h2>
              <p className="mt-1 text-sm text-slate-500">
                {totalElements} matches / {stayLabel}
              </p>
            </div>
            {isBusy ? <Loader2 className="h-5 w-5 animate-spin text-teal-700" /> : null}
          </div>

          {error ? (
            <div className="m-4 rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700">
              {error}
            </div>
          ) : null}

          <div className="grid gap-3 p-4">
            {results.length === 0 && !error ? (
              <div className="rounded-lg border border-dashed border-slate-300 bg-slate-50/70 p-8 text-center text-sm text-slate-500">
                Run a search to load hotels from the API Gateway.
              </div>
            ) : null}

            {results.map((hotel) => {
              const imageSrc = resolveImageUrl(hotel.imageUrl);
              const reviewSummary = searchReviewSummaries[hotel.id];
              const reviewRating = ratingFromSummary(reviewSummary);

              return (
                <button
                  className={`rounded-lg border p-4 text-left shadow-sm transition hover:-translate-y-0.5 hover:border-teal-400 hover:bg-teal-50/50 hover:shadow-md ${
                    selectedHotelId === hotel.id
                      ? "border-teal-600 bg-teal-50 shadow-md shadow-teal-900/10"
                      : "border-slate-200 bg-white"
                  }`}
                  key={hotel.id}
                  onClick={() => loadHotelDetail(hotel.id)}
                  type="button"
                >
                  {imageSrc ? (
                    <img
                      alt={`${hotel.name} hotel`}
                      className="mb-3 h-36 w-full rounded-md object-cover shadow-sm"
                      src={imageSrc}
                    />
                  ) : null}
                  <div className="flex flex-wrap items-start justify-between gap-3">
                    <div>
                      <h3 className="text-lg font-bold">{hotel.name}</h3>
                      <p className="mt-1 flex items-center gap-1 text-sm text-slate-500">
                        <MapPin className="h-4 w-4 text-teal-700" />
                        {hotel.destination}
                      </p>
                    </div>
                    <div className="min-w-[7rem] text-right">
                      <PriceDisplay
                        discounted={Boolean(authSession)}
                        price={hotel.lowestPricePerNight}
                      />
                      <p className="text-xs text-slate-500">per night</p>
                    </div>
                  </div>
                  <p className="mt-3 line-clamp-2 text-sm leading-6 text-slate-600">
                    {hotel.description}
                  </p>
                  <div className="mt-3 flex flex-wrap gap-2 text-xs font-semibold text-slate-600">
                    <span className="inline-flex items-center gap-1 rounded-full bg-amber-100 px-2 py-1 text-amber-800">
                      <Star className="h-3.5 w-3.5 fill-current" />
                      {reviewRating === null ? "No reviews" : reviewRating.toFixed(1)}
                    </span>
                    {reviewRating !== null ? (
                      <span className="rounded-full bg-slate-100 px-2 py-1">
                        {reviewSummary.totalComments} reviews
                      </span>
                    ) : null}
                    <span className="rounded-full bg-slate-100 px-2 py-1">
                      {hotel.availableRoomTypes} room types
                    </span>
                  </div>
                </button>
              );
            })}
          </div>
        </div>

        <aside className="min-h-[32rem] rounded-lg border border-slate-200/80 bg-white/95 shadow-[0_18px_45px_rgba(15,23,42,0.08)]">
          <div className="border-b border-slate-200/80 px-4 py-4">
            <h2 className="text-base font-bold">Hotel detail</h2>
            <p className="mt-1 text-sm text-slate-500">{stayLabel}</p>
          </div>

          {detail ? (
            <div className="grid gap-5 p-4">
              {booking ? (
                <div
                  aria-live="polite"
                  className="rounded-md border border-teal-200/80 bg-teal-50/90 p-4 text-sm text-teal-900 shadow-sm"
                >
                  <div className="flex items-start gap-3">
                    <CheckCircle2 className="mt-0.5 h-5 w-5 shrink-0 text-teal-700" />
                    <div>
                      <p className="font-bold">Booking confirmed</p>
                      <p className="mt-1">
                        {booking.hotelName} / {booking.roomType} / {formatMoney(booking.totalPrice)}
                      </p>
                      <p className="mt-1 text-xs text-teal-800">
                        Reservation {booking.id} is {booking.status}.
                      </p>
                    </div>
                  </div>
                </div>
              ) : null}

              {bookingError ? (
                <div
                  aria-live="polite"
                  className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700 shadow-sm"
                >
                  {bookingError}
                </div>
              ) : null}

              {resolveImageUrl(detail.imageUrl) ? (
                <img
                  alt={`${detail.name} hotel`}
                  className="h-64 w-full rounded-md object-cover shadow-sm"
                  src={resolveImageUrl(detail.imageUrl) ?? undefined}
                />
              ) : null}

              <div>
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div>
                    <h3 className="text-2xl font-bold">{detail.name}</h3>
                    <p className="mt-2 flex items-center gap-2 text-sm text-slate-500">
                      <MapPin className="h-4 w-4 text-teal-700" />
                      {detail.address}
                    </p>
                  </div>
                  <span className="inline-flex items-center gap-1 rounded-full bg-amber-100 px-3 py-1 text-sm font-bold text-amber-800">
                    <Star className="h-4 w-4 fill-current" />
                    {ratingFromSummary(reviews.summary) === null
                      ? "No reviews"
                      : ratingFromSummary(reviews.summary)?.toFixed(1)}
                  </span>
                </div>
                <p className="mt-4 text-sm leading-6 text-slate-600">{detail.description}</p>
              </div>

              <div className="flex flex-wrap gap-2">
                {detail.amenities.map((amenity) => (
                  <span
                    className="rounded-full border border-slate-200 bg-slate-50/90 px-3 py-1 text-xs font-semibold text-slate-600"
                    key={amenity}
                  >
                    {amenity}
                  </span>
                ))}
              </div>

              <div className="grid gap-3">
                {detail.rooms.map((room) => {
                  const availableCount = room.minAvailableCount ?? room.totalCount;
                  const roomIsBooking = status === "booking" && bookingRoomId === room.id;

                  return (
                    <article className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm transition hover:border-teal-200 hover:shadow-md" key={room.id}>
                      <div className="flex flex-wrap items-start justify-between gap-3">
                        <div>
                          <h4 className="flex items-center gap-2 font-bold">
                            <BedDouble className="h-4 w-4 text-teal-700" />
                            {room.roomType}
                          </h4>
                          <p className="mt-2 text-sm text-slate-500">
                            Capacity {room.capacity} / {room.totalCount} total rooms
                          </p>
                        </div>
                        <div className="text-right">
                          <PriceDisplay
                            className="text-lg"
                            discounted={Boolean(authSession)}
                            price={room.pricePerNight}
                          />
                          <p className="text-xs text-slate-500">per night</p>
                        </div>
                      </div>
                      <p className="mt-3 text-sm font-semibold text-teal-700">
                        {availableCount} available for these dates
                      </p>
                      <button
                        className="mt-4 inline-flex h-10 w-full items-center justify-center gap-2 rounded-md bg-teal-700 px-4 text-sm font-bold text-white shadow-md shadow-teal-900/15 transition hover:-translate-y-0.5 hover:bg-teal-800 disabled:cursor-not-allowed disabled:opacity-60 disabled:hover:translate-y-0 sm:w-auto"
                        disabled={isBusy || availableCount < 1}
                        onClick={() => createBooking(room)}
                        type="button"
                      >
                        {roomIsBooking ? (
                          <Loader2 className="h-4 w-4 animate-spin" />
                        ) : (
                          <CheckCircle2 className="h-4 w-4" />
                        )}
                        Book this room
                      </button>
                    </article>
                  );
                })}
              </div>

              <section className="grid gap-4 rounded-lg border border-slate-200 bg-slate-50/80 p-4">
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <div>
                    <h3 className="flex items-center gap-2 text-base font-bold">
                      <MessageSquare className="h-4 w-4 text-teal-700" />
                      Guest comments
                    </h3>
                    <p className="mt-1 text-sm text-slate-500">
                      {reviews.summary?.totalComments ?? reviews.totalComments} guest reviews
                    </p>
                  </div>
                  <div className="rounded-md bg-white px-3 py-2 text-right shadow-sm ring-1 ring-slate-200">
                    <p className="text-2xl font-bold text-slate-950">
                      {(reviews.summary?.averageRating ?? 0).toFixed(1)}
                    </p>
                    <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">
                      average
                    </p>
                  </div>
                </div>

                {reviewsLoading ? (
                  <div className="flex items-center gap-2 rounded-md bg-white p-3 text-sm text-slate-500 shadow-sm ring-1 ring-slate-100">
                    <Loader2 className="h-4 w-4 animate-spin text-teal-700" />
                    Loading comments and rating graphs
                  </div>
                ) : null}

                {reviewsError ? (
                  <div
                    aria-live="polite"
                    className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700"
                  >
                    {reviewsError}
                  </div>
                ) : null}

                {canCommentOnSelectedHotel ? (
                  <form
                    className="grid gap-3 rounded-lg bg-white p-4 shadow-sm ring-1 ring-slate-200"
                    onSubmit={createComment}
                  >
                    <div className="flex flex-wrap items-start justify-between gap-3">
                      <div>
                        <h4 className="text-sm font-bold">Add your comment</h4>
                        <p className="mt-1 text-xs text-slate-500">
                          Reservation {booking?.id}
                        </p>
                      </div>
                      <label className="grid gap-1 text-xs font-bold uppercase tracking-wide text-slate-600">
                        Overall
                        <select
                          className="h-9 rounded-md border border-slate-200 bg-white px-2 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                          onChange={(event) => setCommentForm({ ...commentForm, overallRating: event.target.value })}
                          value={commentForm.overallRating}
                        >
                          {[5, 4, 3, 2, 1].map((rating) => (
                            <option key={rating} value={rating}>
                              {rating}
                            </option>
                          ))}
                        </select>
                      </label>
                    </div>

                    <textarea
                      className="min-h-24 rounded-md border border-slate-200 bg-white px-3 py-2 text-sm outline-none transition placeholder:text-slate-400 focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                      onChange={(event) => setCommentForm({ ...commentForm, comment: event.target.value })}
                      placeholder="Share your stay experience..."
                      required
                      value={commentForm.comment}
                    />

                    <div className="grid gap-2 sm:grid-cols-4">
                      {(["cleanliness", "location", "staff", "comfort"] as const).map((service) => (
                        <label className="grid gap-1 text-xs font-bold uppercase tracking-wide text-slate-600" key={service}>
                          {formatServiceName(service)}
                          <select
                            className="h-9 rounded-md border border-slate-200 bg-white px-2 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                            onChange={(event) => setCommentForm({ ...commentForm, [service]: event.target.value })}
                            value={commentForm[service]}
                          >
                            {[5, 4, 3, 2, 1].map((rating) => (
                              <option key={rating} value={rating}>
                                {rating}
                              </option>
                            ))}
                          </select>
                        </label>
                      ))}
                    </div>

                    {commentMessage ? (
                      <div aria-live="polite" className="rounded-md border border-teal-200 bg-teal-50 p-3 text-sm text-teal-800">
                        {commentMessage}
                      </div>
                    ) : null}

                    {commentError ? (
                      <div aria-live="polite" className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700">
                        {commentError}
                      </div>
                    ) : null}

                    <button
                      className="inline-flex h-10 items-center justify-center gap-2 rounded-md bg-teal-700 px-4 text-sm font-bold text-white shadow-md shadow-teal-900/15 transition hover:-translate-y-0.5 hover:bg-teal-800 disabled:cursor-not-allowed disabled:opacity-60 disabled:hover:translate-y-0 sm:w-fit"
                      disabled={commentBusy || !commentForm.comment.trim()}
                      type="submit"
                    >
                      {commentBusy ? <Loader2 className="h-4 w-4 animate-spin" /> : <MessageSquare className="h-4 w-4" />}
                      Post comment
                    </button>
                  </form>
                ) : (
                  <div className="rounded-md border border-dashed border-slate-300 bg-white p-4 text-sm text-slate-500">
                    Book this hotel first to add a guest comment.
                  </div>
                )}

                {!canCommentOnSelectedHotel && commentError ? (
                  <div aria-live="polite" className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700">
                    {commentError}
                  </div>
                ) : null}

                <div className="grid gap-4 xl:grid-cols-2">
                  <div className="rounded-lg bg-white p-4 shadow-sm ring-1 ring-slate-200">
                    <h4 className="flex items-center gap-2 text-sm font-bold">
                      <BarChart3 className="h-4 w-4 text-teal-700" />
                      Star distribution
                    </h4>
                    <div className="mt-4 grid gap-2">
                      {[5, 4, 3, 2, 1].map((rating) => {
                        const count = reviews.starDistribution[String(rating)] ?? 0;
                        const maxCount = Math.max(
                          1,
                          ...Object.values(reviews.starDistribution).map(Number),
                        );
                        const width = `${Math.round((count / maxCount) * 100)}%`;

                        return (
                          <div className="grid grid-cols-[2rem_1fr_2rem] items-center gap-2" key={rating}>
                            <span className="text-xs font-bold text-slate-600">{rating}</span>
                            <div className="h-2 overflow-hidden rounded-full bg-slate-100">
                              <div className="h-full rounded-full bg-amber-400" style={{ width }} />
                            </div>
                            <span className="text-right text-xs font-semibold text-slate-500">
                              {count}
                            </span>
                          </div>
                        );
                      })}
                    </div>
                  </div>

                  <div className="rounded-lg bg-white p-4 shadow-sm ring-1 ring-slate-200">
                    <h4 className="flex items-center gap-2 text-sm font-bold">
                      <BarChart3 className="h-4 w-4 text-teal-700" />
                      Service ratings
                    </h4>
                    <div className="mt-4 grid gap-3">
                      {Object.entries(reviews.serviceDistribution).length === 0 ? (
                        <p className="text-sm text-slate-500">No service ratings yet.</p>
                      ) : null}

                      {Object.entries(reviews.serviceDistribution).map(([service, summary]) => {
                        const width = `${Math.round((summary.averageRating / 5) * 100)}%`;

                        return (
                          <div className="grid gap-1" key={service}>
                            <div className="flex items-center justify-between gap-2 text-xs">
                              <span className="font-bold text-slate-700">
                                {formatServiceName(service)}
                              </span>
                              <span className="text-slate-500">
                                {summary.averageRating.toFixed(1)} / 5 from {summary.count}
                              </span>
                            </div>
                            <div className="h-2 overflow-hidden rounded-full bg-slate-100">
                              <div className="h-full rounded-full bg-teal-600 transition-all" style={{ width }} />
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                </div>

                <div className="grid gap-3">
                  {reviews.comments.length === 0 ? (
                    <div className="rounded-md border border-dashed border-slate-300 bg-white p-4 text-sm text-slate-500">
                      No comments have been added for this hotel yet.
                    </div>
                  ) : null}

                  {reviews.comments.map((item) => (
                    <article className="rounded-lg bg-white p-4 shadow-sm ring-1 ring-slate-200" key={item.commentId}>
                      <div className="flex flex-wrap items-center justify-between gap-2">
                        <span className="inline-flex items-center gap-1 rounded-full bg-amber-100 px-2 py-1 text-xs font-bold text-amber-800">
                          <Star className="h-3.5 w-3.5 fill-current" />
                          {item.overallRating.toFixed(1)}
                        </span>
                        <span className="text-xs text-slate-500">
                          {new Date(item.createdAt).toLocaleDateString()}
                        </span>
                      </div>
                      <p className="mt-3 text-sm leading-6 text-slate-700">{item.comment}</p>
                      <div className="mt-3 flex flex-wrap gap-2">
                        {Object.entries(item.serviceRatings).map(([service, rating]) => (
                          <span
                            className="rounded-full border border-slate-200 bg-slate-50 px-2 py-1 text-xs font-semibold text-slate-600"
                            key={service}
                          >
                            {formatServiceName(service)} {rating}/5
                          </span>
                        ))}
                      </div>
                    </article>
                  ))}
                </div>
              </section>
            </div>
          ) : (
            <div className="grid min-h-[28rem] place-items-center p-6 text-center text-sm text-slate-500">
              Select a hotel to load date-aware room options.
            </div>
          )}
        </aside>
      </section>

      <section className="border-t border-slate-200/70 bg-white/75 px-5 py-10 backdrop-blur sm:px-8 lg:px-10">
        <div className="mx-auto grid max-w-7xl gap-5 lg:grid-cols-[1.25fr_0.75fr]">
          <div className="rounded-lg border border-slate-200/80 bg-[#eef7f4]/90 p-4 shadow-[0_18px_45px_rgba(15,23,42,0.08)]">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div>
                <p className="text-xs font-bold uppercase tracking-wide text-teal-700">
                  Map view
                </p>
                <h2 className="mt-1 flex items-center gap-2 text-xl font-bold">
                  <Map className="h-5 w-5 text-teal-700" />
                  Hotel coordinates
                </h2>
              </div>
              {mapLoading ? <Loader2 className="h-5 w-5 animate-spin text-teal-700" /> : null}
            </div>

            <div className="relative mt-4 h-80 overflow-hidden rounded-md border border-teal-100 bg-white shadow-inner">
              <div className="absolute inset-0 bg-[linear-gradient(90deg,rgba(15,118,110,0.08)_1px,transparent_1px),linear-gradient(rgba(15,118,110,0.08)_1px,transparent_1px)] bg-[size:32px_32px]" />
              <div className="absolute inset-0 bg-[radial-gradient(circle_at_70%_20%,rgba(245,158,11,0.16),transparent_16rem),radial-gradient(circle_at_20%_80%,rgba(20,184,166,0.18),transparent_14rem)]" />
              <div className="absolute left-4 top-4 rounded-md bg-white/90 px-3 py-2 text-xs font-semibold text-slate-600 shadow-sm ring-1 ring-slate-200">
                Search result coordinates
              </div>

              {results.length === 0 || !bounds ? (
                <div className="absolute inset-0 grid place-items-center p-6 text-center text-sm text-slate-500">
                  Search hotels to plot result coordinates.
                </div>
              ) : null}

              {bounds
                ? results.map((hotel) => {
                    const position = mapPinPosition(hotel, bounds);
                    const selected = selectedHotelId === hotel.id;

                    return (
                      <button
                        aria-label={`Open ${hotel.name} map detail`}
                        className={`absolute -translate-x-1/2 -translate-y-1/2 rounded-full border-2 p-2 shadow-md transition hover:scale-110 ${
                          selected
                            ? "border-slate-950 bg-teal-600 text-white shadow-teal-900/30"
                            : "border-white bg-slate-950 text-white"
                        }`}
                        key={hotel.id}
                        onClick={() => loadHotelDetail(hotel.id)}
                        style={position}
                        type="button"
                      >
                        <MapPin className="h-4 w-4" />
                      </button>
                    );
                  })
                : null}
            </div>
          </div>

          <aside className="rounded-lg border border-slate-200/80 bg-slate-50/90 p-4 shadow-[0_18px_45px_rgba(15,23,42,0.08)]">
            <div className="flex items-start justify-between gap-3">
              <div>
                <h3 className="text-base font-bold">Selected location</h3>
                <p className="mt-1 text-sm text-slate-500">
                  Coordinates are confirmed through the hotel map endpoint.
                </p>
              </div>
              <MapPin className="h-5 w-5 text-teal-700" />
            </div>

            {mapError ? (
              <div className="mt-4 rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700">
                {mapError}
              </div>
            ) : null}

            {mapHotel ? (
              <div className="mt-4 grid gap-3">
                <div className="rounded-lg bg-white p-4 shadow-sm ring-1 ring-slate-200">
                  <p className="text-xs font-bold uppercase tracking-wide text-teal-700">
                    Map endpoint
                  </p>
                  <h4 className="mt-2 text-lg font-bold">{mapHotel.name}</h4>
                  <p className="mt-1 text-sm text-slate-500">{mapHotel.destination}</p>
                  <div className="mt-4 grid gap-2 text-sm">
                    <div className="flex items-center justify-between gap-3">
                      <span className="text-slate-500">Latitude</span>
                      <span className="font-mono font-semibold">{Number(mapHotel.latitude).toFixed(6)}</span>
                    </div>
                    <div className="flex items-center justify-between gap-3">
                      <span className="text-slate-500">Longitude</span>
                      <span className="font-mono font-semibold">{Number(mapHotel.longitude).toFixed(6)}</span>
                    </div>
                  </div>
                  <a
                    className="mt-4 inline-flex h-10 items-center justify-center gap-2 rounded-md bg-slate-950 px-3 text-sm font-bold text-white shadow-md shadow-slate-950/15 transition hover:-translate-y-0.5 hover:bg-teal-700 focus:outline-none focus:ring-2 focus:ring-teal-200"
                    href={googleMapsUrl(mapHotel.latitude, mapHotel.longitude)}
                    rel="noreferrer"
                    target="_blank"
                  >
                    <ExternalLink className="h-4 w-4" />
                    Open in Google Maps
                  </a>
                </div>

                {selectedResult ? (
                  <div className="rounded-lg bg-white p-4 shadow-sm ring-1 ring-slate-200">
                    <p className="text-xs font-bold uppercase tracking-wide text-slate-500">
                      Search/detail data
                    </p>
                    <div className="mt-3 grid gap-2 text-sm">
                      <div className="flex items-center justify-between gap-3">
                        <span className="text-slate-500">Latitude</span>
                        <span className="font-mono font-semibold">
                          {Number(selectedResult.latitude).toFixed(6)}
                        </span>
                      </div>
                      <div className="flex items-center justify-between gap-3">
                        <span className="text-slate-500">Longitude</span>
                        <span className="font-mono font-semibold">
                          {Number(selectedResult.longitude).toFixed(6)}
                        </span>
                      </div>
                    </div>
                  </div>
                ) : null}
              </div>
            ) : (
              <div className="mt-4 rounded-md border border-dashed border-slate-300 bg-white p-5 text-sm text-slate-500">
                Select a hotel to load the map endpoint.
              </div>
            )}
          </aside>
        </div>
      </section>

      <section className="border-t border-slate-200/70 bg-transparent px-5 py-10 sm:px-8 lg:px-10">
        <div className="mx-auto grid max-w-7xl gap-5 lg:grid-cols-[0.85fr_1.15fr]">
          <div>
            <p className="text-xs font-bold uppercase tracking-wide text-teal-700">
              AI Agent
            </p>
            <h2 className="mt-2 flex items-center gap-2 text-2xl font-bold">
              <Bot className="h-6 w-6 text-teal-700" />
              Travel assistant chat
            </h2>
            <p className="mt-3 max-w-xl text-sm leading-6 text-slate-600">
              The chat calls the AI Agent service through the API Gateway. The agent is expected to use project APIs for search and booking flows.
            </p>

            <label className="mt-5 grid gap-1 text-xs font-bold uppercase tracking-wide text-slate-600">
              Session id
              <input
                className="h-11 rounded-md border border-slate-200 bg-white px-3 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                onChange={(event) => setAiSessionId(event.target.value)}
                value={aiSessionId}
              />
            </label>
          </div>

          <div className="rounded-lg border border-slate-200/80 bg-white/95 shadow-[0_18px_45px_rgba(15,23,42,0.08)]">
            <div className="flex items-center justify-between gap-3 border-b border-slate-200/80 px-4 py-4">
              <div>
                <h3 className="text-base font-bold">Chat</h3>
                <p className="mt-1 text-sm text-slate-500">{apiBaseUrl}/api/v1/ai/chat</p>
              </div>
              {aiLoading ? <Loader2 className="h-5 w-5 animate-spin text-teal-700" /> : null}
            </div>

            <div className="grid max-h-[28rem] gap-3 overflow-y-auto p-4">
              {aiMessages.map((message, index) => (
                <div
                  className={`max-w-[85%] rounded-lg px-3 py-2 text-sm leading-6 shadow-sm ${
                    message.role === "user"
                      ? "ml-auto bg-slate-950 text-white"
                      : "mr-auto bg-slate-100 text-slate-800 ring-1 ring-slate-200"
                  }`}
                  key={`${message.role}-${index}`}
                >
                  {message.content}
                </div>
              ))}

              {aiError ? (
                <div aria-live="polite" className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700">
                  {aiError}
                </div>
              ) : null}
            </div>

            <form className="grid gap-3 border-t border-slate-200/80 p-4 sm:grid-cols-[1fr_auto]" onSubmit={sendAiMessage}>
              <label className="sr-only" htmlFor="ai-message">
                Message
              </label>
              <textarea
                className="min-h-12 rounded-md border border-slate-200 bg-white px-3 py-2 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                id="ai-message"
                onChange={(event) => setAiInput(event.target.value)}
                placeholder="Ask for hotels, dates, guests, or a booking flow..."
                value={aiInput}
              />
              <button
                className="inline-flex h-12 items-center justify-center gap-2 rounded-md bg-teal-700 px-4 text-sm font-bold text-white shadow-md shadow-teal-900/15 transition hover:-translate-y-0.5 hover:bg-teal-800 disabled:cursor-not-allowed disabled:opacity-60 disabled:hover:translate-y-0"
                disabled={aiLoading || !aiInput.trim()}
                type="submit"
              >
                {aiLoading ? <Loader2 className="h-4 w-4 animate-spin" /> : <Send className="h-4 w-4" />}
                Send
              </button>
            </form>
          </div>
        </div>
      </section>

      <section className="border-t border-slate-800 bg-slate-950 px-5 py-10 text-white shadow-[inset_0_1px_0_rgba(255,255,255,0.06)] sm:px-8 lg:px-10">
        <div className="mx-auto grid max-w-7xl gap-5">
          <div className="flex flex-wrap items-end justify-between gap-4">
            <div>
              <p className="text-xs font-bold uppercase tracking-wide text-teal-300">
                Admin operations
              </p>
              <h2 className="mt-2 flex items-center gap-2 text-2xl font-bold">
                <Building2 className="h-6 w-6 text-teal-300" />
                Hotel dashboard
              </h2>
              <p className="mt-2 max-w-3xl text-sm leading-6 text-slate-300">
                Create or update hotels, rooms, and date availability through the API Gateway.
              </p>
            </div>
            <div className="rounded-md border border-white/10 bg-white/5 px-3 py-2 text-sm text-slate-300 shadow-sm">
              {apiBaseUrl}/api/v1/admin
            </div>
          </div>

          <div className="grid gap-3 rounded-lg border border-white/10 bg-white/[0.06] p-4 shadow-[0_18px_45px_rgba(0,0,0,0.18)]">
            <div className="flex items-center gap-2 text-sm font-bold">
              <KeyRound className="h-4 w-4 text-teal-300" />
              Admin authentication
            </div>
            <div className="grid gap-3 lg:grid-cols-[1.4fr_1fr]">
              <label className="grid gap-1 text-xs font-bold uppercase tracking-wide text-slate-300">
                Authorization bearer token
                <input
                  className="h-11 rounded-md border border-white/10 bg-slate-900/90 px-3 text-sm text-white outline-none transition placeholder:text-slate-500 focus:border-teal-300 focus:ring-2 focus:ring-teal-300/20"
                  onChange={(event) => setAdminAuth({ ...adminAuth, token: event.target.value })}
                  placeholder="Bearer eyJ..."
                  value={adminAuth.token}
                />
              </label>
              <label className="grid gap-1 text-xs font-bold uppercase tracking-wide text-slate-300">
                Optional X-User-Id
                <input
                  className="h-11 rounded-md border border-white/10 bg-slate-900/90 px-3 text-sm text-white outline-none transition placeholder:text-slate-500 focus:border-teal-300 focus:ring-2 focus:ring-teal-300/20"
                  onChange={(event) => setAdminAuth({ ...adminAuth, userId: event.target.value })}
                  placeholder="Supabase user id"
                  value={adminAuth.userId}
                />
              </label>
            </div>
          </div>

          {adminMessage ? (
            <div aria-live="polite" className="rounded-md border border-teal-300/30 bg-teal-300/10 p-3 text-sm text-teal-100">
              {adminMessage}
            </div>
          ) : null}

          {adminError ? (
            <div aria-live="polite" className="rounded-md border border-red-300/30 bg-red-400/10 p-3 text-sm text-red-100">
              {adminError}
            </div>
          ) : null}

          <div className="grid gap-5 xl:grid-cols-3">
            <form
              className="grid gap-3 rounded-lg border border-white/10 bg-white p-4 text-slate-950 shadow-[0_18px_45px_rgba(0,0,0,0.18)]"
              onSubmit={(event) => {
                event.preventDefault();
                createAdminHotel();
              }}
            >
              <div>
                <h3 className="text-base font-bold">Hotel</h3>
                <p className="mt-1 text-sm text-slate-500">Create a new hotel or update an existing one.</p>
              </div>

              <label className="grid gap-1 text-xs font-bold uppercase tracking-wide text-slate-600">
                Hotel id for update
                <input
                  className="h-10 rounded-md border border-slate-200 bg-white px-3 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                  onChange={(event) => setAdminHotelForm({ ...adminHotelForm, hotelId: event.target.value })}
                  value={adminHotelForm.hotelId}
                />
              </label>

              <label className="grid gap-1 text-xs font-bold uppercase tracking-wide text-slate-600">
                Name
                <input
                  className="h-10 rounded-md border border-slate-200 bg-white px-3 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                  onChange={(event) => setAdminHotelForm({ ...adminHotelForm, name: event.target.value })}
                  required
                  value={adminHotelForm.name}
                />
              </label>

              <label className="grid gap-1 text-xs font-bold uppercase tracking-wide text-slate-600">
                Description
                <textarea
                  className="min-h-20 rounded-md border border-slate-200 bg-white px-3 py-2 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                  onChange={(event) => setAdminHotelForm({ ...adminHotelForm, description: event.target.value })}
                  value={adminHotelForm.description}
                />
              </label>

              <div className="grid gap-3 sm:grid-cols-2">
                <label className="grid gap-1 text-xs font-bold uppercase tracking-wide text-slate-600">
                  Destination
                  <input
                    className="h-10 rounded-md border border-slate-200 bg-white px-3 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                    onChange={(event) => setAdminHotelForm({ ...adminHotelForm, destination: event.target.value })}
                    required
                    value={adminHotelForm.destination}
                  />
                </label>
                <label className="grid gap-1 text-xs font-bold uppercase tracking-wide text-slate-600">
                  Star rating
                  <input
                    className="h-10 rounded-md border border-slate-200 bg-white px-3 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                    max="5"
                    min="1"
                    onChange={(event) => setAdminHotelForm({ ...adminHotelForm, starRating: event.target.value })}
                    required
                    step="0.1"
                    type="number"
                    value={adminHotelForm.starRating}
                  />
                </label>
              </div>

              <label className="grid gap-1 text-xs font-bold uppercase tracking-wide text-slate-600">
                Address
                <input
                  className="h-10 rounded-md border border-slate-200 bg-white px-3 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                  onChange={(event) => setAdminHotelForm({ ...adminHotelForm, address: event.target.value })}
                  required
                  value={adminHotelForm.address}
                />
              </label>

              <div className="grid gap-3 sm:grid-cols-2">
                <label className="grid gap-1 text-xs font-bold uppercase tracking-wide text-slate-600">
                  Latitude
                  <input
                    className="h-10 rounded-md border border-slate-200 bg-white px-3 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                    onChange={(event) => setAdminHotelForm({ ...adminHotelForm, latitude: event.target.value })}
                    required
                    type="number"
                    value={adminHotelForm.latitude}
                  />
                </label>
                <label className="grid gap-1 text-xs font-bold uppercase tracking-wide text-slate-600">
                  Longitude
                  <input
                    className="h-10 rounded-md border border-slate-200 bg-white px-3 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                    onChange={(event) => setAdminHotelForm({ ...adminHotelForm, longitude: event.target.value })}
                    required
                    type="number"
                    value={adminHotelForm.longitude}
                  />
                </label>
              </div>

              <label className="grid gap-1 text-xs font-bold uppercase tracking-wide text-slate-600">
                Amenities
                <input
                  className="h-10 rounded-md border border-slate-200 bg-white px-3 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                  onChange={(event) => setAdminHotelForm({ ...adminHotelForm, amenities: event.target.value })}
                  value={adminHotelForm.amenities}
                />
              </label>

              <div className="grid gap-2 rounded-md border border-slate-200 bg-slate-50/80 p-3">
                <label className="grid gap-1 text-xs font-bold uppercase tracking-wide text-slate-600">
                  Hotel image
                  <input
                    accept="image/jpeg,image/png,image/webp,image/gif"
                    className="block w-full rounded-md border border-slate-200 bg-white text-sm text-slate-700 file:mr-3 file:h-10 file:border-0 file:bg-teal-700 file:px-3 file:text-sm file:font-bold file:text-white file:transition hover:file:bg-teal-800"
                    key={adminImageInputKey}
                    onChange={(event) => setAdminImageFile(event.target.files?.[0] ?? null)}
                    type="file"
                  />
                </label>
                <button
                  className="inline-flex h-10 items-center justify-center gap-2 rounded-md border border-teal-700 bg-white px-3 text-sm font-bold text-teal-800 transition hover:-translate-y-0.5 hover:bg-teal-50 hover:shadow-sm disabled:opacity-60 disabled:hover:translate-y-0"
                  disabled={adminBusy !== null || !adminImageFile}
                  onClick={uploadAdminHotelImage}
                  type="button"
                >
                  {adminBusy === "upload-image" ? <Loader2 className="h-4 w-4 animate-spin" /> : <ImagePlus className="h-4 w-4" />}
                  Upload image
                </button>
              </div>

              <div className="grid gap-2 sm:grid-cols-2">
                <button
                  className="inline-flex h-10 items-center justify-center gap-2 rounded-md bg-teal-700 px-3 text-sm font-bold text-white shadow-md shadow-teal-900/15 transition hover:-translate-y-0.5 hover:bg-teal-800 disabled:opacity-60 disabled:hover:translate-y-0"
                  disabled={adminBusy !== null}
                  type="submit"
                >
                  {adminBusy === "create-hotel" ? <Loader2 className="h-4 w-4 animate-spin" /> : <Plus className="h-4 w-4" />}
                  Create
                </button>
                <button
                  className="inline-flex h-10 items-center justify-center gap-2 rounded-md border border-slate-200 bg-white px-3 text-sm font-bold text-slate-700 transition hover:-translate-y-0.5 hover:bg-slate-50 hover:shadow-sm disabled:opacity-60 disabled:hover:translate-y-0"
                  disabled={adminBusy !== null}
                  onClick={updateAdminHotel}
                  type="button"
                >
                  {adminBusy === "update-hotel" ? <Loader2 className="h-4 w-4 animate-spin" /> : <Save className="h-4 w-4" />}
                  Update
                </button>
              </div>
            </form>

            <form
              className="grid gap-3 rounded-lg border border-white/10 bg-white p-4 text-slate-950 shadow-[0_18px_45px_rgba(0,0,0,0.18)]"
              onSubmit={(event) => {
                event.preventDefault();
                createAdminRoom();
              }}
            >
              <div>
                <h3 className="text-base font-bold">Room</h3>
                <p className="mt-1 text-sm text-slate-500">Create or update room inventory and nightly price.</p>
              </div>

              <label className="grid gap-1 text-xs font-bold uppercase tracking-wide text-slate-600">
                Hotel id
                <input
                  className="h-10 rounded-md border border-slate-200 bg-white px-3 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                  onChange={(event) => setAdminRoomForm({ ...adminRoomForm, hotelId: event.target.value })}
                  required
                  value={adminRoomForm.hotelId}
                />
              </label>

              <label className="grid gap-1 text-xs font-bold uppercase tracking-wide text-slate-600">
                Room id for update
                <input
                  className="h-10 rounded-md border border-slate-200 bg-white px-3 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                  onChange={(event) => setAdminRoomForm({ ...adminRoomForm, roomId: event.target.value })}
                  value={adminRoomForm.roomId}
                />
              </label>

              <label className="grid gap-1 text-xs font-bold uppercase tracking-wide text-slate-600">
                Room type
                <input
                  className="h-10 rounded-md border border-slate-200 bg-white px-3 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                  onChange={(event) => setAdminRoomForm({ ...adminRoomForm, roomType: event.target.value })}
                  required
                  value={adminRoomForm.roomType}
                />
              </label>

              <div className="grid gap-3 sm:grid-cols-3">
                <label className="grid gap-1 text-xs font-bold uppercase tracking-wide text-slate-600">
                  Capacity
                  <input
                    className="h-10 rounded-md border border-slate-200 bg-white px-3 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                    min="1"
                    onChange={(event) => setAdminRoomForm({ ...adminRoomForm, capacity: event.target.value })}
                    required
                    type="number"
                    value={adminRoomForm.capacity}
                  />
                </label>
                <label className="grid gap-1 text-xs font-bold uppercase tracking-wide text-slate-600">
                  Total
                  <input
                    className="h-10 rounded-md border border-slate-200 bg-white px-3 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                    min="1"
                    onChange={(event) => setAdminRoomForm({ ...adminRoomForm, totalCount: event.target.value })}
                    required
                    type="number"
                    value={adminRoomForm.totalCount}
                  />
                </label>
                <label className="grid gap-1 text-xs font-bold uppercase tracking-wide text-slate-600">
                  Price
                  <input
                    className="h-10 rounded-md border border-slate-200 bg-white px-3 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                    min="0.01"
                    onChange={(event) => setAdminRoomForm({ ...adminRoomForm, pricePerNight: event.target.value })}
                    required
                    step="0.01"
                    type="number"
                    value={adminRoomForm.pricePerNight}
                  />
                </label>
              </div>

              <div className="grid gap-2 sm:grid-cols-2">
                <button
                  className="inline-flex h-10 items-center justify-center gap-2 rounded-md bg-teal-700 px-3 text-sm font-bold text-white shadow-md shadow-teal-900/15 transition hover:-translate-y-0.5 hover:bg-teal-800 disabled:opacity-60 disabled:hover:translate-y-0"
                  disabled={adminBusy !== null}
                  type="submit"
                >
                  {adminBusy === "create-room" ? <Loader2 className="h-4 w-4 animate-spin" /> : <Plus className="h-4 w-4" />}
                  Create
                </button>
                <button
                  className="inline-flex h-10 items-center justify-center gap-2 rounded-md border border-slate-200 bg-white px-3 text-sm font-bold text-slate-700 transition hover:-translate-y-0.5 hover:bg-slate-50 hover:shadow-sm disabled:opacity-60 disabled:hover:translate-y-0"
                  disabled={adminBusy !== null}
                  onClick={updateAdminRoom}
                  type="button"
                >
                  {adminBusy === "update-room" ? <Loader2 className="h-4 w-4 animate-spin" /> : <Save className="h-4 w-4" />}
                  Update
                </button>
              </div>
            </form>

            <form
              className="grid content-start gap-3 rounded-lg border border-white/10 bg-white p-4 text-slate-950 shadow-[0_18px_45px_rgba(0,0,0,0.18)]"
              onSubmit={(event) => {
                event.preventDefault();
                upsertAdminAvailability();
              }}
            >
              <div>
                <h3 className="text-base font-bold">Availability</h3>
                <p className="mt-1 text-sm text-slate-500">Upsert room availability for a date window.</p>
              </div>

              <label className="grid gap-1 text-xs font-bold uppercase tracking-wide text-slate-600">
                Room id
                <input
                  className="h-10 rounded-md border border-slate-200 bg-white px-3 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                  onChange={(event) => setAvailabilityForm({ ...availabilityForm, roomId: event.target.value })}
                  required
                  value={availabilityForm.roomId}
                />
              </label>

              <div className="grid gap-3 sm:grid-cols-2">
                <label className="grid gap-1 text-xs font-bold uppercase tracking-wide text-slate-600">
                  Start date
                  <input
                    className="h-10 rounded-md border border-slate-200 bg-white px-3 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                    onChange={(event) => setAvailabilityForm({ ...availabilityForm, startDate: event.target.value })}
                    required
                    type="date"
                    value={availabilityForm.startDate}
                  />
                </label>
                <label className="grid gap-1 text-xs font-bold uppercase tracking-wide text-slate-600">
                  End date
                  <input
                    className="h-10 rounded-md border border-slate-200 bg-white px-3 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                    onChange={(event) => setAvailabilityForm({ ...availabilityForm, endDate: event.target.value })}
                    required
                    type="date"
                    value={availabilityForm.endDate}
                  />
                </label>
              </div>

              <label className="grid gap-1 text-xs font-bold uppercase tracking-wide text-slate-600">
                Available count
                <input
                  className="h-10 rounded-md border border-slate-200 bg-white px-3 text-sm outline-none transition focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                  min="0"
                  onChange={(event) => setAvailabilityForm({ ...availabilityForm, availableCount: event.target.value })}
                  required
                  type="number"
                  value={availabilityForm.availableCount}
                />
              </label>

              <button
                className="inline-flex h-10 items-center justify-center gap-2 rounded-md bg-teal-700 px-3 text-sm font-bold text-white shadow-md shadow-teal-900/15 transition hover:-translate-y-0.5 hover:bg-teal-800 disabled:opacity-60 disabled:hover:translate-y-0"
                disabled={adminBusy !== null}
                type="submit"
              >
                {adminBusy === "availability" ? <Loader2 className="h-4 w-4 animate-spin" /> : <CalendarDays className="h-4 w-4" />}
                Save availability
              </button>
            </form>
          </div>
        </div>
      </section>
    </main>
  );
}
