import { useEffect, useState, type FormEvent } from 'react'

import {
  changePassword,
  getProfile,
  resetSimulator,
  updateProfile,
} from '../features/profile/profileService'
import {
  getRiskSettings,
  updateRiskSettings,
} from '../features/risk/riskSettingsService'
import type { UserProfile } from '../types/profile'
import type { RiskSettings } from '../types/risk'
import { getApiErrorMessage } from '../utils/apiError'

const inputClass =
  'mt-2 w-full rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-white outline-none focus:border-rocket-500'

export function SettingsPage() {
  const [profile, setProfile] = useState<UserProfile | null>(null)
  const [risk, setRisk] = useState<RiskSettings | null>(null)
  const [passwords, setPasswords] = useState({
    currentPassword: '',
    newPassword: '',
    confirmNewPassword: '',
  })
  const [confirmText, setConfirmText] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [busySection, setBusySection] = useState('')
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  useEffect(() => {
    Promise.all([getProfile(), getRiskSettings()])
      .then(([profileData, riskData]) => {
        setProfile(profileData)
        setRisk(riskData)
      })
      .catch((requestError) =>
        setError(
          getApiErrorMessage(requestError, 'Unable to load account settings'),
        ),
      )
      .finally(() => setIsLoading(false))
  }, [])

  function begin(section: string) {
    setBusySection(section)
    setError('')
    setMessage('')
  }

  async function handleProfileSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!profile) return
    begin('profile')
    try {
      const updated = await updateProfile({
        fullName: profile.fullName,
        phoneNumber: profile.phoneNumber ?? '',
        country: profile.country ?? '',
        preferredCurrency: profile.preferredCurrency,
      })
      setProfile(updated)
      setMessage('Profile updated successfully')
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to update profile'))
    } finally {
      setBusySection('')
    }
  }

  async function handlePasswordSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (passwords.newPassword !== passwords.confirmNewPassword) {
      setError('New password and confirmation do not match')
      return
    }
    begin('password')
    try {
      await changePassword(passwords)
      setPasswords({
        currentPassword: '',
        newPassword: '',
        confirmNewPassword: '',
      })
      setMessage('Password changed successfully')
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to change password'))
    } finally {
      setBusySection('')
    }
  }

  async function handleRiskSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!risk) return
    begin('risk')
    try {
      setRisk(await updateRiskSettings(risk))
      setMessage('Risk settings updated successfully')
    } catch (requestError) {
      setError(
        getApiErrorMessage(requestError, 'Unable to update risk settings'),
      )
    } finally {
      setBusySection('')
    }
  }

  async function handleReset(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    begin('reset')
    try {
      await resetSimulator({ confirmText })
      setConfirmText('')
      setMessage('Simulator reset successfully')
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to reset simulator'))
    } finally {
      setBusySection('')
    }
  }

  if (isLoading) {
    return (
      <div className="grid min-h-[60vh] place-items-center text-slate-400">
        Loading settings...
      </div>
    )
  }

  if (!profile || !risk) {
    return (
      <div className="mx-auto max-w-5xl px-6 py-14 text-red-300">
        {error || 'Account settings are unavailable'}
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-6xl px-6 py-14">
      <header>
        <p className="text-sm font-semibold uppercase tracking-[0.2em] text-rocket-400">
          Account controls
        </p>
        <h1 className="mt-3 text-4xl font-bold text-white">Settings</h1>
        <p className="mt-3 text-slate-400">
          Manage your profile, password, trading limits, and virtual simulator.
        </p>
      </header>

      {error && (
        <p className="mt-6 rounded-xl border border-red-500/30 bg-red-500/10 px-4 py-3 text-red-300">
          {error}
        </p>
      )}
      {message && (
        <p className="mt-6 rounded-xl border border-rocket-500/30 bg-rocket-500/10 px-4 py-3 text-rocket-300">
          {message}
        </p>
      )}

      <div className="mt-8 grid gap-6 lg:grid-cols-2">
        <SettingsCard title="Profile Information">
          <form onSubmit={handleProfileSubmit} className="grid gap-4">
            <label>
              <span className="text-sm text-slate-300">Full Name</span>
              <input
                required
                value={profile.fullName}
                onChange={(event) =>
                  setProfile({ ...profile, fullName: event.target.value })
                }
                className={inputClass}
              />
            </label>
            <label>
              <span className="text-sm text-slate-300">Email</span>
              <input
                readOnly
                value={profile.email}
                className={`${inputClass} cursor-not-allowed opacity-60`}
              />
            </label>
            <label>
              <span className="text-sm text-slate-300">Phone Number</span>
              <input
                value={profile.phoneNumber ?? ''}
                onChange={(event) =>
                  setProfile({ ...profile, phoneNumber: event.target.value })
                }
                className={inputClass}
              />
            </label>
            <label>
              <span className="text-sm text-slate-300">Country</span>
              <input
                value={profile.country ?? ''}
                onChange={(event) =>
                  setProfile({ ...profile, country: event.target.value })
                }
                className={inputClass}
              />
            </label>
            <label>
              <span className="text-sm text-slate-300">
                Preferred Currency
              </span>
              <input
                required
                maxLength={3}
                value={profile.preferredCurrency}
                onChange={(event) =>
                  setProfile({
                    ...profile,
                    preferredCurrency: event.target.value.toUpperCase(),
                  })
                }
                className={`${inputClass} uppercase`}
              />
            </label>
            <SubmitButton
              busy={busySection === 'profile'}
              label="Save Profile"
            />
          </form>
        </SettingsCard>

        <SettingsCard title="Change Password">
          <form onSubmit={handlePasswordSubmit} className="grid gap-4">
            {[
              ['currentPassword', 'Current Password'],
              ['newPassword', 'New Password'],
              ['confirmNewPassword', 'Confirm New Password'],
            ].map(([field, label]) => (
              <label key={field}>
                <span className="text-sm text-slate-300">{label}</span>
                <input
                  type="password"
                  required
                  minLength={field === 'currentPassword' ? undefined : 8}
                  value={passwords[field as keyof typeof passwords]}
                  onChange={(event) =>
                    setPasswords({
                      ...passwords,
                      [field]: event.target.value,
                    })
                  }
                  className={inputClass}
                />
              </label>
            ))}
            <SubmitButton
              busy={busySection === 'password'}
              label="Change Password"
            />
          </form>
        </SettingsCard>

        <SettingsCard title="Trading Risk Settings">
          <form onSubmit={handleRiskSubmit} className="grid gap-4">
            <label>
              <span className="text-sm text-slate-300">Max Order Value</span>
              <input
                type="number"
                required
                min="0.01"
                max="100000"
                step="0.01"
                value={risk.maxOrderValue}
                onChange={(event) =>
                  setRisk({
                    ...risk,
                    maxOrderValue: Number(event.target.value),
                  })
                }
                className={inputClass}
              />
            </label>
            <label>
              <span className="text-sm text-slate-300">Max Daily Trades</span>
              <input
                type="number"
                required
                min={1}
                max={200}
                value={risk.maxDailyTrades}
                onChange={(event) =>
                  setRisk({
                    ...risk,
                    maxDailyTrades: Number(event.target.value),
                  })
                }
                className={inputClass}
              />
            </label>
            <Toggle
              label="Allow Limit Orders"
              checked={risk.allowLimitOrders}
              onChange={(checked) =>
                setRisk({ ...risk, allowLimitOrders: checked })
              }
            />
            <Toggle
              label="Allow Stop-Loss Orders"
              checked={risk.allowStopLossOrders}
              onChange={(checked) =>
                setRisk({ ...risk, allowStopLossOrders: checked })
              }
            />
            <SubmitButton
              busy={busySection === 'risk'}
              label="Save Risk Settings"
            />
          </form>
        </SettingsCard>

        <article className="rounded-2xl border border-red-500/30 bg-red-500/5 p-6">
          <h2 className="text-xl font-bold text-red-300">Reset Simulator</h2>
          <p className="mt-3 text-sm leading-6 text-slate-300">
            This resets your virtual balance and clears current holdings. It
            does not delete your account or historical orders and trades.
          </p>
          <form onSubmit={handleReset} className="mt-5 grid gap-4">
            <label>
              <span className="text-sm text-slate-300">
                Type RESET MY SIMULATOR
              </span>
              <input
                required
                value={confirmText}
                onChange={(event) => setConfirmText(event.target.value)}
                className={inputClass}
              />
            </label>
            <button
              type="submit"
              disabled={
                busySection === 'reset' ||
                confirmText !== 'RESET MY SIMULATOR'
              }
              className="rounded-xl bg-red-500 px-5 py-3 font-semibold text-white hover:bg-red-400 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {busySection === 'reset' ? 'Resetting...' : 'Reset Simulator'}
            </button>
          </form>
        </article>
      </div>
    </div>
  )
}

function SettingsCard({
  title,
  children,
}: {
  title: string
  children: React.ReactNode
}) {
  return (
    <article className="rounded-2xl border border-slate-800 bg-slate-900/60 p-6">
      <h2 className="text-xl font-bold text-white">{title}</h2>
      <div className="mt-5">{children}</div>
    </article>
  )
}

function SubmitButton({ busy, label }: { busy: boolean; label: string }) {
  return (
    <button
      type="submit"
      disabled={busy}
      className="rounded-xl bg-rocket-500 px-5 py-3 font-semibold text-slate-950 hover:bg-rocket-400 disabled:opacity-60"
    >
      {busy ? 'Saving...' : label}
    </button>
  )
}

function Toggle({
  label,
  checked,
  onChange,
}: {
  label: string
  checked: boolean
  onChange: (checked: boolean) => void
}) {
  return (
    <label className="flex items-center justify-between rounded-xl border border-slate-800 bg-slate-950/60 px-4 py-3">
      <span className="text-sm text-slate-300">{label}</span>
      <input
        type="checkbox"
        checked={checked}
        onChange={(event) => onChange(event.target.checked)}
        className="size-5 accent-rocket-500"
      />
    </label>
  )
}
