"use client";

import { Controller, SubmitHandler, useForm } from "react-hook-form";
import MDEditor from "@uiw/react-md-editor";
import { useEffect } from "react";
import React from "react";
import UploadInput, { FileInput } from "../components/inputs/UploadInput";
import useTimelines from "../hooks/useTimelines";
import useCurrentUser from "../hooks/useCurrentUser";
import useCharacters from "../hooks/useCharacters";
import { useToast } from "@/components/hooks/use-toast";
import { useArticle } from "../hooks/useArticles";
import Select from "react-select";

type SelectType = {
    label: string;
    value?: number;
};

type Inputs = {
    title: string;
    body: string;
    uploadedAttachments: FileInput[];
    timeline: SelectType;
    characters: SelectType[];
};

type ArticleFormProps = {
    id?: number;
};

export default function ArticleForm({ id }: ArticleFormProps) {
    const {
        register,
        handleSubmit,
        watch,
        setValue,
        reset,
        formState,
        control,
    } = useForm<Inputs>({
        defaultValues: {
            title: "ExampleTitle",
            body: "ExampleBody",
            uploadedAttachments: [],
        },
    });

    const {
        user,
        loading,
        loggedOut,
        mutate: mutateCurrentUser,
    } = useCurrentUser();
    const userId = user && user.id;
    const { timelines, mutate: mutateTimelines } = useTimelines(userId);
    const { characters, mutate: mutateCharacters } = useCharacters(userId);
    const { article, mutate: mutateArticle } = useArticle(id?.toString() || "");
    const { toast } = useToast();
    const onSubmit: SubmitHandler<Inputs> = async (data) => {
        if (id) {
            await handleUpdateArticle(id, data);
        } else {
            await handleCreateArticle(data);
        }
    };

    async function handleCreateArticle(data: Inputs) {
        const formData = new FormData();
        formData.append("title", data.title);
        formData.append("body", data.body);
        if (data.timeline && data.timeline.value) {
            formData.append("timelineId", data.timeline.value.toString());
        }
        if (data.characters) {
            data.characters.forEach((c) => {
                if (c.value) {
                    formData.append("characters", c.value?.toString());
                }
            });
        }
        data.uploadedAttachments
            .filter((it) => it.data)
            .map((it) => {
                it.data && formData.append("uploadedAttachments", it.data);
                formData.append(
                    "uploadedAttachmentsMetadata",
                    JSON.stringify(it.metadata)
                );
            });

        const response = await fetch("http://localhost:7070/api/articles", {
            method: "POST",
            credentials: "include",
            body: formData,
        });

        if (response.ok) {
            toast({
                title: "Article Successfully Created",
                description: data.title,
            });
        }
    }

    async function handleUpdateArticle(id: number, data: Inputs) {
        const formData = new FormData();
        formData.append("id", id.toString());
        formData.append("title", data.title);
        formData.append("body", data.body);
        if (data.timeline && data.timeline.value) {
            formData.append("timelineId", data.timeline.value.toString());
        }
        if (data.characters) {
            data.characters.forEach((c) => {
                if (c.value) {
                    formData.append("characters", c.value?.toString());
                }
            });
        }
        data.uploadedAttachments.map((it) => {
            if (!it.metadata.id) {
                it.data && formData.append("uploadedAttachments", it.data);
            }
            formData.append(
                "uploadedAttachmentsMetadata",
                JSON.stringify(it.metadata)
            );
        });

        const response = await fetch("http://localhost:7070/api/articles", {
            method: "PUT",
            credentials: "include",
            body: formData,
        });

        if (response.ok) {
            toast({
                title: "Article Successfully Updated",
                description: data.title,
            });
            mutateArticle();
        }
    }

    useEffect(() => {
        if (id && article) {
            const { title, body, timeline, characters, attachments } = article;
            setValue("title", title);
            setValue("body", body);
            if (timeline) {
                setValue("timeline", {
                    label: timeline.name,
                    value: timeline.id,
                });
            }

            setValue(
                "characters",
                characters.map((c) => ({
                    label: c.name,
                    value: c.id,
                }))
            );

            if (attachments && attachments.length > 0) {
                setValue(
                    "uploadedAttachments",
                    attachments.map(
                        (a, index) =>
                            ({
                                id: index.toString(),
                                url: `http://localhost:7070/api/files/serve/${a.storageId}/${a.name}`,
                                metadata: {
                                    id: a.id,
                                    caption: a.caption,
                                    delete: false,
                                },
                            } as FileInput)
                    )
                );
            }
        }
    }, [article, id, setValue]);

    const timelineOptions =
        timelines?.map((timeline) => ({
            label: timeline.name,
            value: timeline.id,
        })) || [];

    const characterOptions =
        characters?.map((character) => ({
            label: character.name,
            value: character.id,
        })) || [];

    return (
        <div>
            <form onSubmit={handleSubmit(onSubmit)}>
                <div className="flex flex-col gap-4 py-4">
                    <label className="flex flex-col" htmlFor="title">
                        Title
                        <input
                            {...register("title", { required: true })}
                            id="title"
                            defaultValue="Untitled"
                        />
                    </label>
                    <label className="flex flex-col" htmlFor="name">
                        Timeline (Optional)
                        {/* <select
                            {...register("timelineId")}
                            disabled={!!!timelines}
                        >
                            <option key={0} value={undefined}>
                                None
                            </option>
                            {timelines &&
                                timelines.map((it) => (
                                    <option key={it.id} value={it.id}>
                                        {it.name}
                                    </option>
                                ))}
                        </select> */}
                        <Controller
                            name="timeline"
                            control={control}
                            render={({ field }) => (
                                <Select
                                    options={[
                                        { label: "None", value: undefined },
                                        ...timelineOptions,
                                    ]}
                                    value={field.value}
                                    onChange={field.onChange}
                                    isDisabled={!!!timelines}
                                />
                            )}
                        />
                    </label>
                    {characters && characters.length > 0 && (
                        <label className="flex flex-col" htmlFor="name">
                            Starring Characters (Optional)
                            {/* <select
                                {...register("characters")}
                                disabled={!!!timelines}
                                multiple
                            >
                                {characters.map((it) => (
                                    <option key={it.id} value={it.id}>
                                        {it.name}
                                    </option>
                                ))}
                            </select> */}
                            <Controller
                                name="characters"
                                control={control}
                                render={({ field }) => (
                                    <Select
                                        options={characterOptions}
                                        value={field.value}
                                        onChange={field.onChange}
                                        isMulti
                                        isDisabled={!!!characters}
                                    />
                                )}
                            />
                        </label>
                    )}

                    <label className="flex flex-col" htmlFor="body">
                        Body
                        <Controller
                            name="body"
                            control={control}
                            defaultValue="Empty body"
                            render={({ field }) => (
                                <div data-color-mode="light">
                                    <MDEditor
                                        value={field.value}
                                        onChange={field.onChange}
                                    />
                                </div>
                            )}
                        />
                    </label>

                    <label className="flex flex-col" htmlFor="">
                        File Attachments
                        <Controller
                            name="uploadedAttachments"
                            control={control}
                            render={({ field }) => (
                                <UploadInput
                                    id="uploadedAttachments"
                                    setFileInputs={(files: FileInput[]) => {
                                        setValue("uploadedAttachments", files);
                                    }}
                                    fileInputs={field.value}
                                />
                            )}
                        />
                    </label>
                </div>
                <button className="button" type="submit">
                    {id ? "Save Changes" : "Submit"}
                </button>
            </form>
        </div>
    );
}
