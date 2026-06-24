export type UserProfile = {
  id: string
  fullName: string
  email: string
  role: string
  phoneNumber: string | null
  country: string | null
  preferredCurrency: string
  createdAt: string
  updatedAt: string
  lastLoginAt: string | null
}

export type UpdateProfileRequest = {
  fullName: string
  phoneNumber: string
  country: string
  preferredCurrency: string
}

export type ChangePasswordRequest = {
  currentPassword: string
  newPassword: string
  confirmNewPassword: string
}

export type ResetAccountRequest = {
  confirmText: string
}
