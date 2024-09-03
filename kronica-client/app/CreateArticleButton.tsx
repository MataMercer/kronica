"use client";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from "@/components/ui/dialog";
import { Controller, SubmitHandler, useForm } from "react-hook-form";
import { refreshArticles } from "./actions";
import MDEditor from "@uiw/react-md-editor";
import { useState } from "react";
import React from "react";
import UploadInput, { FileInput } from "./components/inputs/UploadInput";

type Inputs = {
    title: string;
    body: string;
    attachments: string[];
    uploadedAttachments: FileInput[];
};

export default function CreateArticleButton() {
    const {
        register,
        handleSubmit,
        watch,
        setValue,
        reset,
        formState: { errors },
        control,
    } = useForm<Inputs>({
        defaultValues: {
            title: "ExampleTitle",
            body: "ExampleBody",
            attachments: [],
            uploadedAttachments: [],
        },
    });

    const [showArticleForm, setShowArticleForm] = useState(false);
    const ref = React.useRef();
    const onSubmit: SubmitHandler<Inputs> = async (data) => {
        const formData = new FormData();
        console.log(data);
        formData.append("title", data.title);
        formData.append("body", data.body);
        formData.append(
            "uploadedAttachments",
            data.uploadedAttachments
                .filter((it) => {
                    return it.data;
                })
                .map((it) => it.data)
        );
        const response = await fetch("http://localhost:7070/api/articles", {
            method: "POST",
            credentials: "include",
            body: formData,
        });

        refreshArticles();
        // setShowArticleForm(false);
        ref.current?.click();
        reset();
    };

    return (
        <Dialog>
            <DialogTrigger asChild ref={ref}>
                <button
                    className="button"
                    onClick={() => {
                        setShowArticleForm(true);
                    }}
                >
                    CREATE ARTICLE
                </button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-[90vw]">
                <form onSubmit={handleSubmit(onSubmit)}>
                    <DialogHeader>
                        <DialogTitle>CREATE ARTICLE</DialogTitle>
                    </DialogHeader>
                    <div className="flex flex-col gap-4 py-4">
                        <label className="flex flex-col" htmlFor="title">
                            Title
                            <input
                                {...register("title", { required: true })}
                                id="title"
                                defaultValue="Untitled"
                            />
                        </label>
                        <label className="flex flex-col" htmlFor="body">
                            Body
                            {/* <textarea
                                    {...register("body", { required: true })}
                                    id="body"
                                    defaultValue="Empty body"
                                    rows={20}
                                /> */}
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
                                        {/* <MDEditor.Markdown
                                                source={field.value}
                                                style={{
                                                    whiteSpace: "pre-wrap",
                                                }}
                                            /> */}
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
                                            setValue(
                                                "uploadedAttachments",
                                                files
                                            );
                                        }}
                                        fileInputs={field.value}
                                    />
                                )}
                            ></Controller>
                        </label>
                    </div>
                    <DialogFooter>
                        <button className="button" type="submit">
                            Submit
                        </button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
}
